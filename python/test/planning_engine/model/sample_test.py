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
from planning_engine.model.hidden_node_class import AbstractNode
from planning_engine.model.sample_class import Sample, Samples


class TestSample(unittest.TestCase):
    def test_initializes_sample_with_valid_parameters(self):
        edges = [HiddenEdge(source_hn_name="hidden1", target_hn_name="hidden2", edge_type=EdgeType.LinkEdge)]
        sample = Sample(
            probability_count=5,
            utility=0.75,
            name="Sample1",
            description="Test sample",
            edges=edges)

        self.assertEqual(sample.probability_count, 5)
        self.assertEqual(sample.utility, 0.75)
        self.assertEqual(sample.name, "Sample1")
        self.assertEqual(sample.description, "Test sample")
        self.assertEqual(sample.edges, edges)

    def test_raises_error_when_probability_count_is_negative(self):
        with self.assertRaises(AssertionError):
            Sample(probability_count=-1, utility=0.75, name="Sample1", description="Test sample", edges=[])

    def test_raises_error_when_probability_count_is_none(self):
        with self.assertRaises(AssertionError):
            Sample(probability_count=None, utility=0.75, name="Sample1", description="Test sample", edges=[])

    def test_raises_error_when_utility_is_none(self):
        with self.assertRaises(AssertionError):
            Sample(probability_count=5, utility=None, name="Sample1", description="Test sample", edges=[])

    def test_converts_sample_to_json_correctly(self):
        edges = [HiddenEdge(source_hn_name="hidden1", target_hn_name="hidden2", edge_type=EdgeType.LinkEdge)]
        sample = Sample(probability_count=5, utility=0.75, name="Sample1", description="Test sample",
                        edges=edges)
        expected_json = {
            "probabilityCount": 5,
            "utility": 0.75,
            "name": "Sample1",
            "description": "Test sample",
            "edges": [edge.to_json() for edge in edges]
        }
        self.assertEqual(sample.to_json(), expected_json)


class TestSamples(unittest.TestCase):
    def __init__(self, *args, **kwargs):
        super(TestSamples, self).__init__(*args, **kwargs)

        self.hidden_nodes = [
            AbstractNode(name="hidden1", description=None),
            AbstractNode(name="hidden2", description=None)
        ]

        self.sample = Sample(
            probability_count=5,
            utility=0.75,
            name="Sample1",
            description="Test sample",
            edges=[HiddenEdge(source_hn_name="hidden1", target_hn_name="hidden2", edge_type=EdgeType.LinkEdge)]
        )

    def test_initializes_samples_with_valid_parameters(self):
        samples_obj = Samples(hidden_nodes=self.hidden_nodes, samples=[self.sample])
        self.assertEqual(samples_obj.hidden_nodes, self.hidden_nodes)
        self.assertEqual(samples_obj.samples, [self.sample])

    def test_raises_error_when_hidden_nodes_list_is_empty(self):
        with self.assertRaises(AssertionError):
            Samples(hidden_nodes=[], samples=[self.sample])

    def test_raises_error_when_hidden_nodes_have_duplicate_names(self):
        hidden_nodes = [AbstractNode(name="hidden1", description=None), AbstractNode(name="hidden1", description=None)]
        with self.assertRaises(AssertionError):
            Samples(hidden_nodes=hidden_nodes, samples=[self.sample])

    def test_raises_error_when_samples_list_is_empty(self):
        with self.assertRaises(AssertionError):
            Samples(hidden_nodes=self.hidden_nodes, samples=[])

    def test_raises_error_when_sample_contains_invalid_hidden_edge_names(self):
        samples = [
            Sample(probability_count=5, utility=0.75, name="Sample1", description="Test sample", edges=[
                HiddenEdge(source_hn_name="hidden1", target_hn_name="invalidNode", edge_type=EdgeType.LinkEdge)
            ])
        ]
        with self.assertRaises(AssertionError):
            Samples(hidden_nodes=self.hidden_nodes, samples=samples)

    def test_converts_samples_to_json_correctly(self):
        samples_obj = Samples(hidden_nodes=self.hidden_nodes, samples=[self.sample])
        expected_json = {
            "hiddenNodes": [node.to_json() for node in self.hidden_nodes],
            "samples": [self.sample.to_json()]
        }
        self.assertEqual(samples_obj.to_json(), expected_json)


if __name__ == '__main__':
    unittest.main()
