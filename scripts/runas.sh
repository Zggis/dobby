#!/bin/bash

#-----------------------------------------------------------------------------------------------------------------------

function ts {
  echo [`date '+%Y-%m-%d %H:%M:%S'`]
}

#-----------------------------------------------------------------------------------------------------------------------

function process_args {
  # These are intended to be global
  PUID=$1
  PGID=$2
  UMASK=$3

  if [[ ! "$PUID" =~ ^[0-9]{1,}$ ]]
  then
    echo "User ID value $PUID is not valid. It must be a whole number"
    exit 1
  fi

  if [[ ! "$PGID" =~ ^[0-9]{1,}$ ]]
  then
    echo "Group ID value $PGID is not valid. It must be a whole number"
    exit 1
  fi

  if [[ ! "$UMASK" =~ ^0[0-7][0-7][0-7]$ ]]
  then
    echo "The umask value $UMASK is not valid. It must be an octal number such as 0022"
    exit 1
  fi
}

#-----------------------------------------------------------------------------------------------------------------------

function create_user {
  local PUID=$1
  local PGID=$2

  USER="user_${PUID}_$PGID"
  GROUP="group_${PUID}_$PGID"

  if grep -q "^[^:]*:[^:]*:$PUID:$PGID:" /etc/passwd >/dev/null 2>&1
  then
    USER=$(grep "^[^:]*:[^:]*:$PUID:$PGID:" /etc/passwd | sed 's/:.*//')

    if [[ $USER == *$'\n'* ]]
    then
      echo "$(ts) ERROR: Found multiple users with the proper user ID and group ID. Exiting..."
      exit 1
    fi

    echo "$(ts) Found existing user \"$USER\" with the proper user ID and group ID. Skipping creation of user and group..."
    return
  fi

  if grep -q "^[^:]*:[^:]*:$PUID:" /etc/passwd >/dev/null 2>&1
  then
    USER=$(grep "^[^:]*:[^:]*:$PUID:" /etc/passwd | sed 's/:.*//')

    if [[ $USER == *$'\n'* ]]
    then
      echo "$(ts) ERROR: Found multiple users with the proper user ID and incorrect group ID. Refusing to modify the group ID. Exiting..."
    else
      echo "$(ts) ERROR: Found user \"$USER\" with the proper user ID but incorrect group ID. Refusing to modify the group ID. Exiting..."
    fi

    exit 1
  fi

  if id -u $USER >/dev/null 2>&1
  then
    echo "$(ts) User \"$USER\" already exists. Skipping creation of new user and group..."
    return
  fi

  echo "$(ts) Creating user \"$USER\" (ID $PUID) and group \"$GROUP\" (ID $PGID) to run the command..."

  # We could be aliasing this new user to some existing user. I assume that's harmless.
  groupadd -o -g $PGID $GROUP
  useradd -o -u $PUID -r -g $GROUP -d /home/$USER -s /sbin/nologin -c "Docker image user" $USER

  mkdir -p /home/$USER
  chown -R $USER:$GROUP /home/$USER
}

#-----------------------------------------------------------------------------------------------------------------------

process_args "$@"

# Shift off the args so that we can exec $@ below
shift; shift; shift

create_user $PUID $PGID

echo "$(ts) Running command as user \"$USER\"..."
umask $UMASK
eval exec /sbin/setuser $USER "$@"
