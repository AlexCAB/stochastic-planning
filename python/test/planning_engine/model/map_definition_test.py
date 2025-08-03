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
| created: 2025-07-21 ||||||||||"""

import unittest

from planning_engine.model.io_node_class import BoolIoNode, IntIoNode
from planning_engine.model.map_definition_class import MapDefinition


class TestMapDefinition(unittest.TestCase):
    def __init__(self, *args, **kwargs):
        super(TestMapDefinition, self).__init__(*args, **kwargs)

        self.bool_io_node = BoolIoNode(name="testBoolNode", acceptable_values=[True, False])
        self.int_io_node = IntIoNode(name="testIntNode", min=0, max=10)

    def test_creates_instance_with_valid_parameters(self):
        map_def = MapDefinition(
            db_name="test_db",
            name="test_map",
            description="Test description",
            input_nodes=[self.bool_io_node],
            output_nodes=[self.int_io_node]
        )
        self.assertEqual(map_def.db_name, "test_db")
        self.assertEqual(map_def.name, "test_map")
        self.assertEqual(map_def.description, "Test description")
        self.assertEqual(map_def.input_nodes, [self.bool_io_node])
        self.assertEqual(map_def.output_nodes, [self.int_io_node])

    def test_raises_error_when_db_name_is_empty(self):
        with self.assertRaises(AssertionError):
            MapDefinition(
                db_name="",
                name="test_map",
                description=None,
                input_nodes=[],
                output_nodes=[]
            )

    def test_raises_error_when_name_is_empty(self):
        with self.assertRaises(AssertionError):
            MapDefinition(
                db_name="test_db",
                name="",
                description=None,
                input_nodes=[],
                output_nodes=[]
            )

    def test_converts_to_json_correctly(self):
        map_def = MapDefinition(
            db_name="test_db",
            name="test_map",
            description="Test description",
            input_nodes=[self.bool_io_node],
            output_nodes=[self.int_io_node]
        )
        expected_json = {
            "dbName": "test_db",
            "name": "test_map",
            "description": "Test description",
            "inputNodes": [node.to_json() for node in map_def.input_nodes],
            "outputNodes": [node.to_json() for node in map_def.output_nodes]
        }
        self.assertEqual(map_def.to_json(), expected_json)


if __name__ == '__main__':
    unittest.main()
