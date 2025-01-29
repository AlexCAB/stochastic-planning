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
| created: 2025-01-30 ||||||||||"""

import unittest
from map.io_node import IoNode

class TestIoNode(unittest.TestCase):

    def test_initializes_correctly_with_valid_parameters(self):
        node = IoNode(int, "test_node", "12345")
        self.assertEqual(node._type, int)
        self.assertEqual(node.name, "test_node")
        self.assertEqual(node.neo4j_id, "12345")
        self.assertIsNone(node.list_values)

    def test_raises_assertion_error_for_invalid_type(self):
        with self.assertRaises(AssertionError):
            IoNode(str, "test_node", "12345")

    def test_raises_assertion_error_for_none_type(self):
        with self.assertRaises(AssertionError):
            IoNode(None, "test_node", "12345")

    def test_raises_assertion_error_for_none_name(self):
        with self.assertRaises(AssertionError):
            IoNode(int, None, "12345")

    def test_raises_assertion_error_for_none_neo4j_id(self):
        with self.assertRaises(AssertionError):
            IoNode(int, "test_node", None)

    def test_raises_assertion_error_for_none_list_values_with_list_type(self):
        with self.assertRaises(AssertionError):
            IoNode(list, "test_node", "12345")

    def test_returns_correct_value_for_bool_type(self):
        node = IoNode(bool, "test_node", "12345")
        self.assertTrue(node.value_for_index(1))
        self.assertFalse(node.value_for_index(0))

    def test_raises_assertion_error_for_invalid_index_bool_type(self):
        node = IoNode(bool, "test_node", "12345")
        with self.assertRaises(AssertionError):
            node.value_for_index(2)

    def test_returns_correct_value_for_int_type(self):
        node = IoNode(int, "test_node", "12345")
        self.assertEqual(node.value_for_index(5), 5)

    def test_returns_correct_value_for_float_type(self):
        node = IoNode(float, "test_node", "12345")
        self.assertEqual(node.value_for_index(5000), 0.5)

    def test_returns_correct_value_for_list_type(self):
        node = IoNode(list, "test_node", "12345", list_values=[10, 20, 30])
        self.assertEqual(node.value_for_index(1), 20)

    def test_raises_assertion_error_for_invalid_index_list_type(self):
        node = IoNode(list, "test_node", "12345", list_values=[10, 20, 30])
        with self.assertRaises(AssertionError):
            node.value_for_index(3)

    def test_returns_correct_index_for_bool_type(self):
        node = IoNode(bool, "test_node", "12345")
        self.assertEqual(node.index_for_value(True), 1)
        self.assertEqual(node.index_for_value(False), 0)

    def test_raises_assertion_error_for_invalid_value_bool_type(self):
        node = IoNode(bool, "test_node", "12345")
        with self.assertRaises(AssertionError):
            node.index_for_value(2)

    def test_returns_correct_index_for_int_type(self):
        node = IoNode(int, "test_node", "12345")
        self.assertEqual(node.index_for_value(5), 5)

    def test_returns_correct_index_for_float_type(self):
        node = IoNode(float, "test_node", "12345")
        self.assertEqual(node.index_for_value(0.5), 5000)

    def test_returns_correct_index_for_list_type(self):
        node = IoNode(list, "test_node", "12345", list_values=[10, 20, 30])
        self.assertEqual(node.index_for_value(20), 1)

    def test_raises_assertion_error_for_invalid_value_list_type(self):
        node = IoNode(list, "test_node", "12345", list_values=[10, 20, 30])
        with self.assertRaises(AssertionError):
            node.index_for_value(40)


if __name__ == "__main__":
    unittest.main()