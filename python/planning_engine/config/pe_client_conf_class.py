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
| created: 2025-07-18 ||||||||||"""


class PeClientConf:
    @staticmethod
    def from_json_file(json_file: str):
        import json
        with open(json_file, 'r', encoding="utf8") as file:
            data = json.load(file)
            return PeClientConf(pe_host=str(data['peHost']), pe_port=int(data['pePort']))

    def __init__(self, pe_host: str, pe_port: int):
        assert pe_host, "Host should be defined"
        assert isinstance(pe_host, str), "Host should be a string"
        assert pe_port, "Port should be defined"
        assert isinstance(pe_port, int), "Port should be an integer"

        self.pe_host: str = pe_host
        self.pe_port: int = pe_port

        self.http_base = f"http://{pe_host}:{pe_port}"
        self.ws_base = f"ws://{pe_host}:{pe_port}"

    def __str__(self):
        return f"PeClientConf(http_base = {self.http_base}, ws_base = {self.ws_base})"

    def __repr__(self):
        return self.__str__()
