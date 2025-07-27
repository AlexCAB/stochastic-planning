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

from planning_engine.model.hidden_edge_class import HiddenEdge
from planning_engine.model.hidden_node_class import HiddenNode
from planning_engine.model.sample_class import Sample


class TestSample(unittest.TestCase):
    def test_initializes_sample_with_valid_parameters(self):
        hidden_nodes = [HiddenNode(name="hidden1"), HiddenNode(name="hidden2")]
        edges = [HiddenEdge(source_hn_name="hidden1", target_hn_name="hidden2", edge_type=EdgeType.LinkEdge)]
        sample = Sample(
            probability_count=5,
            utility=0.75,
            name="Sample1",
            description="Test sample",
            hidden_nodes=hidden_nodes,
            edges=edges)

        self.assertEqual(sample.probability_count, 5)
        self.assertEqual(sample.utility, 0.75)
        self.assertEqual(sample.name, "Sample1")
        self.assertEqual(sample.description, "Test sample")
        self.assertEqual(sample.hidden_nodes, hidden_nodes)
        self.assertEqual(sample.edges, edges)

    def test_raises_error_when_probability_count_is_negative(self):
        with self.assertRaises(AssertionError):
            Sample(probability_count=-1, utility=0.75, name="Sample1", description="Test sample", hidden_nodes=[],
                   edges=[])

    def test_raises_error_when_probability_count_is_none(self):
        with self.assertRaises(AssertionError):
            Sample(probability_count=None, utility=0.75, name="Sample1", description="Test sample", hidden_nodes=[],
                   edges=[])

    def test_raises_error_when_utility_is_none(self):
        with self.assertRaises(AssertionError):
            Sample(probability_count=5, utility=None, name="Sample1", description="Test sample", hidden_nodes=[],
                   edges=[])

    def test_converts_sample_to_json_correctly(self):
        hidden_nodes = [HiddenNode(name="hidden1"), HiddenNode(name="hidden2")]
        edges = [HiddenEdge(source_hn_name="hidden1", target_hn_name="hidden2", edge_type=EdgeType.LinkEdge)]
        sample = Sample(probability_count=5, utility=0.75, name="Sample1", description="Test sample",
                        hidden_nodes=hidden_nodes, edges=edges)
        expected_json = {
            "probabilityCount": 5,
            "utility": 0.75,
            "name": "Sample1",
            "description": "Test sample",
            "hiddenNodes": [node.to_json() for node in hidden_nodes],
            "edges": [edge.to_json() for edge in edges]
        }
        self.assertEqual(sample.to_json(), expected_json)


if __name__ == '__main__':
    unittest.main()
