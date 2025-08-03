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


class TestConcreteNode(unittest.TestCase):
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

    def test_equality_returns_true_for_identical_concrete_nodes(self):
        node1 = ConcreteNode(name="Node1", description="Description1", io_node_name="IO1", value=42)
        node2 = ConcreteNode(name="Node1", description="Description1", io_node_name="IO1", value=42)
        self.assertTrue(node1 == node2)

    def test_equality_returns_false_when_comparing_with_non_concrete_node(self):
        node = ConcreteNode(name="Node1", description="Description1", io_node_name="IO1", value=42)
        other = AbstractNode(name="Node1", description="Description1")
        self.assertFalse(node == other)


class TestAbstractNode(unittest.TestCase):
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

    def test_equality_returns_true_for_identical_abstract_nodes(self):
        node1 = AbstractNode(name="Node1", description="Description1")
        node2 = AbstractNode(name="Node1", description="Description1")
        self.assertTrue(node1 == node2)

    def test_equality_returns_false_when_comparing_abstract_node_with_non_abstract_node(self):
        node = AbstractNode(name="Node1", description="Description1")
        other = ConcreteNode(name="Node1", description="Description1", io_node_name="IO1", value=42)
        self.assertFalse(node == other)

if __name__ == '__main__':
    unittest.main()
