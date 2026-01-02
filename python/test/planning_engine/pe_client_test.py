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

from planning_engine.model.added_sample_class import AddedSample
from planning_engine.model.hidden_edge_class import HiddenEdge, EdgeType
from planning_engine.model.hidden_node_class import ConcreteNode, AbstractNode
from planning_engine.model.io_node_class import BoolIoNode, FloatIoNode, IntIoNode, ListStrIoNode
from planning_engine.model.map_definition_class import MapDefinition
from planning_engine.model.sample_class import Sample, Samples
from planning_engine.pe_client_class import PeClient
from planning_engine.config.pe_client_conf_class import PeClientConf

logging.basicConfig(level=logging.INFO)


class TestPeClient(unittest.TestCase):
    def __init__(self, *args, **kwargs):
        super(TestPeClient, self).__init__(*args, **kwargs)

        self.config = PeClientConf.from_json_file("config/pe_client_config.json")
        self.client = PeClient(self.config)

    @unittest.skip("No need to kill the planning engine in tests")
    def test_kill_planning_engine(self):
        self.client.kill_planning_engine()

    def test_init_map(self):
        self.client.reset_map()

        definition = MapDefinition(
            db_name="test-db",
            name="pe-client-test",
            description="Test map for PeClient",
            input_nodes=[
                BoolIoNode(name="boolDef", acceptable_values=[True, False]),
                FloatIoNode(name="floatDef", min=0.0, max=1.0)
            ],
            output_nodes=[
                IntIoNode(name="intDef", min=0, max=10),
                ListStrIoNode(name="listStrDef", elements=["a", "b", "c"])
            ]
        )

        map_info = self.client.init_map(definition)

        self.assertIsNotNone(map_info)
        self.assertEqual(map_info.db_name, definition.db_name)
        self.assertEqual(map_info.map_name, definition.name)
        self.assertEqual(map_info.num_input_nodes, len(definition.input_nodes))
        self.assertEqual(map_info.num_output_nodes, len(definition.output_nodes))
        self.assertEqual(map_info.num_hidden_nodes, 0)

    def test_load_map(self):
        self.client.reset_map()

        db_name = "test-db"
        map_info = self.client.load_map(db_name)
        self.assertIsNotNone(map_info)
        self.assertEqual(map_info.db_name, db_name)

    def test_add_samples(self):
        hidden_nodes = [
            ConcreteNode(name="con-hn-1", description="Concrete 1", io_node_name="boolDef", value=True),
            AbstractNode(name="abs-hn-2", description="Abstract 2")
        ]

        edges = [HiddenEdge(source_hn_name="con-hn-1", target_hn_name="abs-hn-2", edge_type=EdgeType.LinkEdge)]

        sample = Sample(
            probability_count=5,
            utility=0.75,
            name="Sample1",
            description="Test sample",
            edges=edges)

        definition = Samples(hidden_nodes, [sample])

        result = self.client.add_samples(definition)

        self.assertIsNotNone(result)
        self.assertEqual(len(result), 1)

        addedSample: AddedSample = result[0]
        self.assertIsNotNone(addedSample.sample_id)
        self.assertEqual(addedSample.sample_name, sample.name)

    def test_build_map_visualisation_ws_app(self):
        def on_message(ws, message):
            print(f"Received message: {message}")

        def on_open(ws):
            print("WebSocket connection opened.")

        def on_ping(ws):
            print("WebSocket ping received.")
            ws.close()

        ws_app = self.client.build_map_visualisation_ws_app(
            on_open=on_open,
            on_message=on_message,
            on_ping=on_ping
        )
        self.assertIsNotNone(ws_app)
        ws_app.run_forever()


if __name__ == '__main__':
    unittest.main()
