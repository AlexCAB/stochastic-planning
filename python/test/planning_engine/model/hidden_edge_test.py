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

from planning_engine.model.hidden_edge_class import HiddenEdge, EdgeType


class TestHiddenEdge(unittest.TestCase):
    def test_creates_hidden_edge_with_valid_parameters(self):
        edge = HiddenEdge(source_hn_name="source1", target_hn_name="target1", edge_type=EdgeType.LinkEdge)
        self.assertEqual(edge.source_hn_name, "source1")
        self.assertEqual(edge.target_hn_name, "target1")
        self.assertEqual(edge.edge_type, EdgeType.LinkEdge)

    def test_raises_error_when_source_is_empty(self):
        with self.assertRaises(AssertionError):
            HiddenEdge(source_hn_name="", target_hn_name="target1", edge_type=EdgeType.LinkEdge)

    def test_raises_error_when_target_is_empty(self):
        with self.assertRaises(AssertionError):
            HiddenEdge(source_hn_name="source1", target_hn_name="", edge_type=EdgeType.LinkEdge)

    def test_raises_error_when_edge_type_is_invalid(self):
        with self.assertRaises(AssertionError):
            HiddenEdge(source_hn_name="source1", target_hn_name="target1", edge_type=None)

    def test_converts_to_json_correctly_with_then_еdge(self):
        edge = HiddenEdge(source_hn_name="source1", target_hn_name="target1", edge_type=EdgeType.ThenEdge)
        expected_json = {
            "sourceHnName": "source1",
            "targetHnName": "target1",
            "edgeType": "Then"
        }
        self.assertEqual(edge.to_json(), expected_json)

    def test_converts_to_json_correctly_with_link_edge(self):
        edge = HiddenEdge(source_hn_name="source1", target_hn_name="target1", edge_type=EdgeType.LinkEdge)
        expected_json = {
            "sourceHnName": "source1",
            "targetHnName": "target1",
            "edgeType": "Link"
        }
        self.assertEqual(edge.to_json(), expected_json)


if __name__ == '__main__':
    unittest.main()
