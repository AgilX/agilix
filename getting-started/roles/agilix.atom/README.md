agilix.atom
=========

An Ansible role that install and configure [Atom](https://atom.io/).

Requirements
------------

Any pre-requisites that may not be covered by Ansible itself or the role should
be mentioned here. For instance, if the role uses the EC2 module, it may be a
good idea to mention in this section that the boto package is required.

Role Variables
--------------

See [defaults/main.yml](defaults/main.yml).

```
atom_ver: Atom version to install
agilix_atom_become_users: List of users for which Atom packages should be installed
atom_upgrade_all_packages: If 'true' all packages will be upgraded to the last version
`̀ `

Example of use:

agilix_atom_become_users:
  - atom_packages:
      - ansible-galaxy@0.2.1
      - behave-step
    username: primael

Dependencies
------------

A list of other roles hosted on Galaxy should go here, plus any details in
regards to parameters that may need to be set for other roles, or variables that
are used from other roles.

Example Playbook
----------------

```
- hosts: all
  roles:
    - agilix.atom
      agilix_atom_become_users:
        - atom_packages:
            - ansible-galaxy@0.2.1
            - behave-step
          username: primael
     atom_ver: 1.32.0
     atom_upgrade_all_packages: false
``̀
Including an example of how to use your role (for instance, with variables
passed in as parameters) is always nice for users too:

    - hosts: servers
      roles:
         - { role: agilix.atom, x: 42 }

License
-------

BSD

Author Information
------------------

Agil'X team
