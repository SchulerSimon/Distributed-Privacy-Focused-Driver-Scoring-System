import socket
import json
import time
import random
import traceback
import subprocess
import os
from settings import *
from subprocess import PIPE, Popen

# debug lets you test commands without the need for a client asking
debug = True
test_request = json.loads(
    b'{"command":"add_score","data":{"time":"16341234","score":"9100000"}}')


class Server_Socket():
    '''
    class that handels connection and communication with the client
    '''

    def __init__(self):
        '''
        starts a new server
        '''
        # gen new auth token
        self.auth_token = gen_new_auth_token()
        # start the acual server
        if not debug:
            self.server_socket = socket.socket()
            self.server_socket.bind(('', port))
            self.server_socket.listen(5)
        else:
            self.server_socket = None

    def get_request(self):
        '''
        w8ts for a request by a client, blocking,
        also keeps the connection alive when there is no exception
        '''
        if not debug:
            # accept incomming connections
            self.client, address = self.server_socket.accept()
            # read available command
            command = self.client.recv(2048)
            if not command:
                # w8 for new connection if connection is dropped
                self.client.close()
                print(string_client_aborted_communication)
                return None

            # try to parse the request from the client
            try:
                request = json.loads(command)
            except:
                # request not parsable
                self.client.close()
                print(string_command_not_understood.format(
                    command))
                return None
        else:
            # in debug mode we just take the predefined request
            request = test_request

        return request

    def send_to_client(self, response):
        if not debug:
            # bug byte-wise object required -> encode()
            self.client.send(response.encode())
            self.client.close()
        print(string_server_to_client_response.format(response))

    def close(self):
        if not debug:
            self.client.close()
            self.server_socket.shutdown
            self.server_socket.close()

    def get_auth_token(self):
        return self.auth_token

    def refresh_auth_token(self):
        self.auth_token = gen_new_auth_token()
        return self.auth_token

    def gen_client_auth_token(self):
        hashlib.sha256(password.encode()).update(self.auth_token)


def main():
    # start server
    server = Server_Socket()
    print(string_server_start)
    # run forever
    while True:
        try:
            # request
            request = server.get_request()

            # if something is wrong we restart and listen again
            if not request:
                continue

            # if the client_auth_token is not valid we restart
            # and listen again
            # TODO implement properly
            # if not debug:
            #    if not request['command'].find('auth_token') == -1 and not request['client_auth_token'] == server.gen_client_auth_token():
            #        server.send(format_response('wrong_auth_token', request[
            #                    'client_auth_token'], status_fail))
            #        break

            # let the user know
            print(string_received_command.format(
                request['command'], str(request['data'])))

            # process request
            if request['command'] == 'get_auth_token':
                get_auth_token(request, server)

            elif request['command'] == 'refresh_auth_token':
                refresh_auth_token(request, server)

            elif request['command'] == 'add_score':
                add_score(request, server)

            elif request['command'] == 'get_times':
                get_times(request, server)

            elif request['command'] == 'get_score':
                get_score(request, server)

            elif request['command'] == 'give_access_right':
                give_access_right(request, server)

            elif request['command'] == 'remove_access_right':
                remove_access_right(request, server)

            elif request['command'] == 'who_has_access_right':
                who_has_access_right(request, server)
            else:
                server.send_to_client(format_response(
                    'command_not_understood', request['command'], status_fail))
                print(string_unknown_command.format(request['command']))
        except KeyboardInterrupt as e:
            server.close()
            break
        except Exception as e:
            print(e)
            traceback.print_tb(e.__traceback__)
        # shutdown the server after execution of command in debug mode
        if debug:
            break
    # close the server_socket after each command
    server.close()


def get_auth_token(request, server):
    '''
    returns a token (randomly generated on startup) that is used to hash the password.
    This way the client can authenticate itself with the correct
    password without making the password public
    '''
    response = format_response('get_auth_token', server.get_auth_token())
    server.send_to_client(response)


def refresh_auth_token(request, server):
    '''
    regenerates the auth_token and returns the new auth_token
    '''
    response = format_response(
        'refresh_auth_token', server.refresh_auth_token())
    server.send_to_client(response)


def add_score(request, server):
    '''
    takes a score and puts it onto the blockchain
    '''
    score = request['data']['score']
    time = request['data']['time']
    # deploy the command
    state, result = deploy_to_burrow(deploy_add_score.format(
        dest=contract_address, time=time, score=score))
    # tell the server if it worked
    if not state:
        server.send_to_client(format_response(
            'add_score', request['data'], status_fail, result))
    else:
        server.send_to_client(format_response('add_score', result))


def get_times(request, server):
    '''
    makes a request to the blockchain and returns the
    times_list (list of all the times for wich exist scores)
    from the blockchain
    '''
    state, result = deploy_to_burrow(deploy_get_times.format(
        dest=contract_address))
    if not state:
        server.send_to_client(format_response(
            'get_times', '', status_fail, result))
    else:
        # load the list of times
        result = json.loads(result['_get_times'])
        temp = []
        # get the score for each one
        for t in result:
            state, score_result = deploy_to_burrow(deploy_get_score.format(
                time=t, dest=contract_address))
            temp += [(t, score_result['_get_score'])]
        # send all back to the client
        server.send_to_client(format_response('get_times', temp))


def get_score(request, server):
    '''
    returns a score for a specific time from the blockchain
    '''
    time = request['data']['time']
    state, result = deploy_to_burrow(deploy_get_score.format(
        dest=contract_address, time=request['data']['time']))
    if not state:
        server.send_to_client(format_response(
            'get_score', time, status_fail, result))
    else:
        server.send_to_client(format_response('get_score', result))


def give_access_right(request, server):
    '''
    takes an address and grants the rights to read information from the userdata
    '''
    address = request['data']['address']
    state, result = deploy_to_burrow(deploy_give_access_right.format(
        dest=contract_address, address=address))
    if not state:
        server.send_to_client(format_response(
            'give_access_right', address, status_fail, result))
    else:
        server.send_to_client(format_response('give_access_right', result))


def remove_access_right(request, server):
    '''
    takes an address and removes the rights to read information from the userdata
    '''
    address = request['data']['address']
    state, result = deploy_to_burrow(deploy_remove_access_right.format(
        dest=contract_address, address=address))
    if not state:
        server.send_to_client(format_response(
            'remove_access_right', address, status_fail, result))
    else:
        server.send_to_client(format_response('remove_access_right', result))


def who_has_access_right(request, server):
    '''
    returns an array of addresses who have access to userdata
    '''
    state, result = deploy_to_burrow(
        deploy_who_has_access_right.format(dest=contract_address))
    if not state:
        server.send_to_client(format_response(
            'who_has_access_right', '', status_fail, result))
    else:
        server.send_to_client(format_response('who_has_access_right', result))


def deploy_to_burrow(yaml_string):
    '''
    deploys the yaml string to the burrow blockchain
    returns a boolean when sucessfull and a string describing 
    the error or the result
    '''
    # write the yaml to a temp file
    with open(burrow_location + deploy_file_name + '.yaml', 'w') as temp_file:
        temp_file.write(yaml_string)
    # cd to burrow location
    cd = 'cd ' + burrow_location
    # deploy with address and temp file
    deploy = './burrow deploy --address {address} {file}'.format(
        address=address, file=deploy_file_name + '.yaml')
    # assemble final command
    command = cd + '\n' + deploy
    # execute
    p = Popen(command, shell=True, stdout=PIPE, stderr=PIPE)
    stdout, stderr = p.communicate()
    # print when debug
    if debug:
        print(stdout, stderr)

    # see if transaction or request worked
    result = str(stderr)
    if result.find('.output.json') != -1:
        # if the result has been written to json file, parse and return it
        with open(burrow_location + deploy_file_name + '.output.json') as result_file:
            return True, json.load(result_file)
    elif result.find('Transaction reverted') != -1:
        return False, 'tx reverted'
    elif result.find('connection refused') != -1:
        return False, 'connection refused'
    elif result.find('not enough data') != -1:
        return False, 'not enough data'
    else:
        return False, 'unspecified error:{}'.format(result)


def gen_new_auth_token(size=64):
    '''
    generates a new auth_token for client server authentication
    '''
    return ''.join([random.choice('0123456789ABCDEF') for x in range(size)])


def format_response(dataname, data, status=status_ok, fail_data=None):
    '''
    formats the response to the clinet and returns a json object
    '''
    response = {'status': status, dataname: data}
    if status == status_fail and fail_data != None:
        response.update({'fail_data': fail_data})
    return json.dumps(response)

if __name__ == '__main__':
    main()
