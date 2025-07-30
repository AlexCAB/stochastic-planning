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
            return PeClientConf(url=data['url'])

    def __init__(self, url: str):
        assert url, "URL should be defined"
        self.url: str = url

    def __str__(self):
        return f"PeClientConf(url = {self.url})"

    def __repr__(self):
        return self.__str__()
