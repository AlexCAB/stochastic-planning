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
| created: 2025-07-19 ||||||||||"""

import os
import unittest

from planning_engine.pe_client_conf_class import PeClientConf


class TestPeClientConf(unittest.TestCase):
    def test_creates_instance_with_valid_url(self):
        conf = PeClientConf(url="http://example.com")
        self.assertEqual(conf.url, "http://example.com")

    def test_raises_error_when_url_is_empty(self):
        with self.assertRaises(AssertionError):
            PeClientConf(url="")

    def test_converts_to_string_correctly(self):
        conf = PeClientConf(url="http://example.com")
        self.assertEqual(str(conf), "PeClientConf(url=http://example.com)")

    def test_creates_instance_from_valid_json_file(self):
        json_file = "test_config.json"
        with open(json_file, "w") as file:
            file.write('{"url": "http://example.com"}')
        conf = PeClientConf.from_json_file(json_file)
        self.assertEqual(conf.url, "http://example.com")
        os.remove(json_file)

    def test_raises_error_for_invalid_json_file(self):
        json_file = "invalid_config.json"
        with open(json_file, "w") as file:
            file.write('{"invalid_key": "http://example.com"}')
        with self.assertRaises(KeyError):
            PeClientConf.from_json_file(json_file)
        os.remove(json_file)


if __name__ == '__main__':
    unittest.main()
