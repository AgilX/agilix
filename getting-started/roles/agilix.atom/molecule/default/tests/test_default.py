import os

import testinfra.utils.ansible_runner

testinfra_hosts = testinfra.utils.ansible_runner.AnsibleRunner(
    os.environ['MOLECULE_INVENTORY_FILE']).get_hosts('all')


def test_hosts_file(host):
    f = host.file('/etc/hosts')

    assert f.exists
    assert f.user == 'root'
    assert f.group == 'root'


def test_config(host):
    config_file = host.file('/home/test_usr/.atom/config.cson')

    assert config_file.exists
    assert config_file.is_file
    assert config_file.user == 'test_usr'
    assert config_file.group == 'test_usr'
    assert oct(config_file.mode) == '0600'
