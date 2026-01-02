#!/usr/bin/python
# -*- coding: utf-8 -*-
r"""|||||||||||||||||||||||||||||||
|| 0 * * * * * * * * * ▲ * * * * ||
|| * ||||||||||| * ||||||||||| * ||
|| * ||  * * * * * ||       || 0 ||
|| * ||||||||||| * ||||||||||| * ||
|| * * ▲ * * 0|| * ||   (< * * * ||
|| * ||||||||||| * ||  ||||||||||||
|| * * * * * * * * *   ||||||||||||
| author: CAB |||||||||||||||||||||
| website: github.com/alexcab |||||
| created: 2026-01-01 ||||||||||"""

import websocket

def main():
    print("################### WebSocket Test ###################")

    url = "ws://localhost:8080/pe/v1/visualization/map"

    ws = websocket.WebSocket()
    ws.connect(url)
    ws.send("Hello, Server")

    def on_message(wsapp, message):
        print(f"on_message type: {type(message)}, msg: {message}")
        wsapp.send("Hello again, Server")

    def on_open(wsapp):
        print(f"on_open")

    def on_reconnect(wsapp):
        print(f"on_reconnect")

    def on_error(wsapp, error):
        print(f"on_error: {error}")

    def on_close(wsapp, a, b):
        print(f"on_close: a = {a}, b = {b}")

    def on_ping(wsapp, data):
        print(f"on_ping, data: {data}")
        wsapp.send(data="pong", opcode=websocket.ABNF.OPCODE_PONG)
        wsapp.close()

    def on_pong():
        print(f"on_pong")

    def on_cont_message(wsapp, data):
        print(f"on_cont_message: data")


    wsapp = websocket.WebSocketApp(
        url,
        on_open=on_open,
        on_reconnect=on_reconnect,
        on_error=on_error,
        on_close=on_close,
        on_ping=on_ping,
        on_pong=on_pong,
        on_cont_message=on_cont_message,
        on_message=on_message
    )
    wsapp.run_forever()


if __name__ == "__main__":
    main()
