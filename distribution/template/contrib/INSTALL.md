# Setup Djigger Startup on Linux

This directory contains a number of helper scripts to properly integrate the
Djigger Collector and Client startup on a server or a desktop environment.

## djigger Collector

If you want to automatically startup the djigger collector service on system
boot it has to be integrated into the init-system. On traditional Linux
distributions such as RHEL 6/CentOS 6 follow the SYSV init approach. More
modern distributions such as RHEL 7/CentOS 7 and Ubuntu 16.04 are using the
`systemd` init system for which the corresponding instructions below should
be used. 

### systemd

The only file required for the `systemd` setup is the
`contrib/djigger-collector.service` is an example for a systemd
[service unit](https://www.freedesktop.org/software/systemd/man/systemd.service.html).
`systemd` allows a service to be setup and controlled in a user session what
might be useful on a developer machine. In a production setup where only one
collector is required which is then managed a with privileged admin account,
the collector service can be setup as regular `systemd` service.

#### Production Setup

Setup the collector daemon as regular `systemd` service. The following tasks
prefixed with a `#` must be done with root permissions. Copy the service file
into the system configuration directory and change the ownership to root:

    # cp contrib/djigger-collector.service /etc/systemd/system/djigger-collector.service
    # chown root:root /etc/systemd/system/djigger-collector.service
    # chmod 0644 /etc/systemd/system/djigger-collector.service

**Service Configuration**

Adjust the content of `/etc/systemd/system/djigger-collector.service` according
to your djigger setup:

* `Exec` (defaults to `/opt/denkbar/djigger/bin/startCollector.sh`): Command
  which is executed when starting the service. Adjust this path to your djigger
  installation.
* `User`/`Group` (defaults to `djigger`): User account which will run the
  collector service. You might need to create the user account and give it
  access to the djigger installation. E.g.

      # useradd -r -d /opt/denkbar/djigger djigger
      # chown -R djigger:djigger /opt/denkbar/djigger

* `Environment` (unset by default): Set any environment variable for the
  collector service. E.g. you must define `JAVA_HOME` if you want to use the
  direct process attach feature of the collector service as following:

      Environment=JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk

  Optionally additional environment variables accepted by the collector startup
  script can be added. E.g. to define additional startup arguments, you can
  set:

      Environment=JAVA_OPTS=-Xmx512m

**Autostart**

To enable autostart on system boot, enable the service via `systemctl`:

    # systemctl enable djigger-collector.service

**Start/Stop Service**

To start/stop/restart the collector service run:

    # systemctl start djigger-collector.service
    # systemctl stop djigger-collector.service
    # systemctl restart djigger-collector.service

Check the status of the collector service:

    # systemctl status djigger-collector.service

**Logging**

The stdout/stderr on a host running `systemd` is usually availble via
"journal". To check the messages run:

    # journalctl -u djigger-collector.service

#### Developer Setup

**Ad-Hoc Startup**

It's possible to run the djigger collector in an ad-hoc mode by simply starting
it with the `bin/startCollector.sh` script. In case you need some customization
the start script will read the following variables from your shell environment:

* `JAVA_HOME`: Root directory of your Java Development Kit installation. This
  must be set if you want to use the direct process attach feature of the
  collector service.

* `JAVA_OPTS`:  Custom Java VM startup arguments.

* `DJIGGER_HOME`: This is automatically detected if unset.

* `DJIGGER_CONFDIR`: Configuration directory where `Collector.xml`,
  `Connections.csv` and such are read.

* `DJIGGER_LIBDIR`: Classpath configuration pointing to the collector
  libraries.

**systemd User Session**

`systemd` supports the management of services within a user session. This
allows a developer to run its individual copy of the collector daemon and still
leverage all the systemd amenities. Run the following commands prefixed with a
`$` with your personal user account:

    $ mkdir -p ~/.config/systemd/user
    $ cp contrib/djigger-collector.service ~/.config/systemd/user/djigger-collector.service
    $ chmod 0644 ~/.config/systemd/user/djigger-collector.service

Adjust the `User=` and `Group=` settings in `~/.config/systemd/user/djigger-collector.service`
with your Linux account name and adjust the `ExecStart=` configuration with the
path to your djigger copy and make sure that your user account has write access
to it. For more details to the remaining configuration options check the
corresponding section in the "Production Setup" chapter.

The `systemctl` commands described above are also valid for the user session
mode of `systemd`. They only have to be extended with the `--user` argument.
E.g. to start your personal djigger collector, run:

    $ systemctl --user start djigger-collector.service

### SYSV Init

**Service Configuration**

Copy the service configuration file to the `/etc/sysconfig` directory and
change its ownership to root:

    # cp contrib/djigger-collector.sysconfig /etc/sysconfig/djigger-collector
    # chown root:root /etc/sysconfig/djigger-collector
    # chmod 0644 /etc/sysconfig/djigger-collector

In this configuration file, you can set various variables which influence the
service startup:

* `DJIGGER_HOME` (defaults to `/opt/denkbar/djigger`) Path to your djigger
  installation.
* `DJIGGER_USER` (defaults to `djigger`): User account which will run the
  collector service. You might need to create the user account and give it
  access to the djigger installation. E.g.
  
      # useradd -r -d /opt/denkbar/djigger djigger
      # chown -R djigger:djigger /opt/denkbar/djigger
  
* `DJIGGER_OUTFILE` (defaults to `/opt/denkbar/djigger/log/collector.out`):
  Logfile where stdout and stderr of the collector process are stored. Set
  this to `/dev/null` if those messages shouldn't be stored.
  
* `JAVA_HOME` (unset by default): Root directory of your Java Development Kit
  installation. This must be defined if you want to use the direct process
  attach feature of the collector service.
  
* `JAVA_OPTS` (unset by default): Custom Java VM startup arguments. Here you
  can e.g. restrict the heap size via `-Xmx512m`
  
**Init Script**

Copy the init script to the `/etc/init.d` directory, change its ownership to
root and make it executable:

     # cp contrib/djigger-collector.initd /etc/init.d/djigger-collector
     # chown root:root /etc/init.d/djigger-collector
     # chmod 0755 /etc/init.d/djigger-collector

**Autostart**

To enable autostart on system boot, register the service via `chkconfig`:

    # chkconfig djigger-collector on

**Start/Stop Service**

To manually start/stop/restart the collector service run:

    # service djigger-collector start
    # service djigger-collector stop
    # service djigger-collector restart

Check the status of the collector service:

    # service djigger-collector status
