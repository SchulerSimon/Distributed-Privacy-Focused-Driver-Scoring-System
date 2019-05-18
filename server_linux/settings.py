'''
this *MUST* be configured by the user
'''
password = "test_pw1234_secure_!"
address = "048AB196B49B328E8D629B44859030513DD7F5FC"
contract_address = "3E82176D45588C20EF1A9AF22C3A79D22901EEE4"

'''
this can be configured by the user
'''
burrow_location = "burrow/"
port = 50505
# no file ending required!
deploy_file_name = "deploy/temp_deploy_file"

'''
this should *NOT* be configured or changed by the user
'''
# constant strings for execution
string_server_start = "server started, listening on port {}".format(port)
string_could_not_start_server = "could not start server, retrying"
string_command_not_understood = "the request: {0} was not in parsable json format"
string_client_aborted_communication = "the clinet aborted the connection"
string_server_to_client_response = "sending response to client: {}"
string_received_command = "server got command: {0}, with data {1}"
string_unknown_command = "server got unknown command: {}"
# values that are needed by the programm at runtime
import hashlib
password_hash = hashlib.sha256(password.encode()).hexdigest()

status_ok = "OK"
status_fail = "FAIL"


'''
if this is changed the calls to burrow fail

we use "call:"" instead of "query-contract:" for view methods for now because burrow has a bug
'''
deploy_add_score = '''jobs:
- name: _add_score
  call:
    destination: {dest}
    function: add_score
    data:
    - {time}
    - {score}
'''

deploy_get_times = '''jobs:
- name: _get_times
  call:
    destination: {dest}
    function: get_times
'''

deploy_get_score = '''jobs:
- name: _get_score
  call:
    destination: {dest}
    function: get_score
    data:
    - {time}
'''

deploy_give_access_right = '''jobs:
- name: _give_access_right
  call:
    destination: {dest}
    function: give_access_right
    data:
    - {address}
'''

deploy_remove_access_right = '''jobs:
- name: _remove_access_right
  call:
    destination: {dest}
    function: remove_access_right
    data:
    - {address}
'''

deploy_who_has_access_right = '''jobs:
- name: _who_has_access_right
  call:
    destination: {dest}
    function: who_has_access_right
'''
