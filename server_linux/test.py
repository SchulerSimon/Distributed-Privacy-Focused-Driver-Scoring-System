import json

data = json.loads(
    b'{"command":"echo","data":{"data1":"test1","data2":"test2"}}')

print(data["command"])
print(data["data"])
