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
| created: 2025-07-28 ||||||||||"""

import unittest

from planning_engine.model.added_sample_class import AddedSample


class TestAddedSample(unittest.TestCase):
    def test_initializes_added_sample_with_valid_parameters(self):
        sample = AddedSample(sample_id=1, sample_name="Sample A")
        self.assertEqual(sample.sample_id, 1)
        self.assertEqual(sample.sample_name, "Sample A")

    def test_initializes_added_sample_with_none_name(self):
        sample = AddedSample(sample_id=2, sample_name=None)
        self.assertEqual(sample.sample_id, 2)
        self.assertIsNone(sample.sample_name)

    def test_raises_error_when_sample_id_is_none(self):
        with self.assertRaises(AssertionError):
            AddedSample(sample_id=None, sample_name="Sample B")

    def test_creates_added_sample_from_valid_json(self):
        json_data = {"sampleId": 3, "sampleName": "Sample C"}
        sample = AddedSample.from_json(json_data)
        self.assertEqual(sample.sample_id, 3)
        self.assertEqual(sample.sample_name, "Sample C")

    def test_creates_added_sample_from_json_with_missing_name(self):
        json_data = {"sampleId": 4}
        sample = AddedSample.from_json(json_data)
        self.assertEqual(sample.sample_id, 4)
        self.assertIsNone(sample.sample_name)

    def test_raises_error_when_json_data_is_empty(self):
        with self.assertRaises(AssertionError):
            AddedSample.from_json({})

    def test_raises_error_when_sample_id_is_missing_in_json(self):
        json_data = {"sampleName": "Sample D"}
        with self.assertRaises(AssertionError):
            AddedSample.from_json(json_data)

    def test_string_representation_is_correct(self):
        sample = AddedSample(sample_id=5, sample_name="Sample E")
        self.assertEqual(str(sample), "AddedSample(sample_id=5, sample_name=Sample E)")


if __name__ == '__main__':
    unittest.main()
