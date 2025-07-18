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

import logging
import unittest

from planning_engine.pe_client_class import PeClient
from planning_engine.pe_client_conf_class import PeClientConf

logging.basicConfig(level=logging.INFO)

class TestPeClientTest(unittest.TestCase):
    def __init__(self, *args, **kwargs):
        super(TestPeClientTest, self).__init__(*args, **kwargs)

        self.config = PeClientConf.from_json_file("config/pe_client_config.json")
        self.client = PeClient(self.config)

    @unittest.skip("No need to kill the planning engine in tests")
    def test_kill_planning_engine(self):
        self.client.kill_planning_engine()


if __name__ == '__main__':
    unittest.main()
