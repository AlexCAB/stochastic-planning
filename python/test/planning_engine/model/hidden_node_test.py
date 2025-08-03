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
| created: 2025-07-27 ||||||||||"""

import unittest
from planning_engine.model.hidden_node_class import ConcreteNode, AbstractNode


class TestHiddenNode(unittest.TestCase):
    def test_creates_concrete_node_with_valid_parameters(self):
        node = ConcreteNode(name="concrete1", description="An concrete node", io_node_name="io1", value=42)
        self.assertEqual(node.name, "concrete1")
        self.assertEqual(node.io_node_name, "io1")
        self.assertEqual(node.description, "An concrete node")
        self.assertEqual(node.value, 42)

    def test_raises_error_when_concrete_node_name_is_empty(self):
        with self.assertRaises(AssertionError):
            ConcreteNode(name="", description=None, io_node_name="io1", value=42)

    def test_raises_error_when_io_node_name_is_empty(self):
        with self.assertRaises(AssertionError):
            ConcreteNode(name="concrete1", description=None, io_node_name="", value=42)

    def test_raises_error_when_value_is_none(self):
        with self.assertRaises(AssertionError):
            ConcreteNode(name="concrete1", description=None, io_node_name="io1", value=None)

    def test_converts_concrete_node_to_json_correctly(self):
        node = ConcreteNode(name="concrete1", description="An concrete node", io_node_name="io1", value=42)
        expected_json = {
            "type": "ConcreteNode",
            "data": {
                "name": "concrete1",
                "description": "An concrete node",
                "ioNodeName": "io1",
                "value": 42
            }
        }
        self.assertEqual(node.to_json(), expected_json)

    def test_converts_abstract_node_to_json_correctly(self):
        node = AbstractNode(name="abstract1", description="An abstract node")
        expected_json = {
            "type": "AbstractNode",
            "data": {
                "name": "abstract1",
                "description": "An abstract node"
            }
        }
        self.assertEqual(node.to_json(), expected_json)


if __name__ == '__main__':
    unittest.main()
