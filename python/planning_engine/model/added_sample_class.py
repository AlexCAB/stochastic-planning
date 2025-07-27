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

from typing import Optional, Dict, Any


class AddedSample:

    @staticmethod
    def from_json(json_data: Dict[str, Any]) -> 'AddedSample':
        assert json_data, "JSON data should not be empty"
        assert 'id' in json_data, "Sample ID should be defined in JSON data"

        sample_id = json_data['id']
        sample_name = json_data.get('name', None)

        return AddedSample(sample_id=sample_id, sample_name=sample_name)

    def __init__(self, sample_id: int, sample_name: Optional[str]):
        assert sample_id is not None, "Sample ID should be defined"

        self.sample_id: int = sample_id
        self.sample_name: Optional[str] = sample_name

    def __str__(self):
        return f"AddedSample(sample_id={self.sample_id}, sample_name={self.sample_name})"

    def __repr__(self):
        return self.__str__()
