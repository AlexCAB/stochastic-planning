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

from planning_engine.model.io_node_class import BoolIoNode, FloatIoNode, IntIoNode, ListStrIoNode
from planning_engine.model.map_definition_class import MapDefinition
from planning_engine.pe_client_class import PeClient
from planning_engine.model.pe_client_conf_class import PeClientConf

logging.basicConfig(level=logging.INFO)

class TestPeClient(unittest.TestCase):
    def __init__(self, *args, **kwargs):
        super(TestPeClient, self).__init__(*args, **kwargs)

        self.config = PeClientConf.from_json_file("config/pe_client_config.json")
        self.client = PeClient(self.config)

    @unittest.skip("No need to kill the planning engine in tests")
    def test_kill_planning_engine(self):
        self.client.kill_planning_engine()

    def test_map_init(self):
        definition = MapDefinition(
            db_name="test-db",
            name="pe-client-test",
            description="Test map for PeClient",
            input_nodes=[
                BoolIoNode(name = "boolDef", acceptable_values = [True, False]),
                FloatIoNode(name = "floatDef", min = 0.0, max = 1.0)
            ],
            output_nodes=[
                IntIoNode(name = "intDef", min = 0, max = 10),
                ListStrIoNode(name = "listStrDef", elements = ["a", "b", "c"])
            ]
        )

        map_info = self.client.init_map(definition)
        self.assertIsNotNone(map_info)
        self.assertEqual(map_info.map_name, definition.name)
        self.assertEqual(map_info.num_input_nodes, len(definition.input_nodes))
        self.assertEqual(map_info.num_output_nodes, len(definition.output_nodes))
        self.assertEqual(map_info.num_hidden_nodes, 0)


if __name__ == '__main__':
    unittest.main()
