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

from enum import Enum
from typing import Dict, Any


class EdgeType(Enum):
    LinkEdge = 1
    ThenEdge = 2


class HiddenEdge:
    def __init__(self, source_hn_name: str, target_hn_name: str, edge_type: EdgeType):
        assert source_hn_name, "Source should be defined"
        assert target_hn_name, "Target should be defined"
        assert isinstance(edge_type, EdgeType), "Edge type should be an instance of EdgeType"

        self.source_hn_name: str = source_hn_name
        self.target_hn_name: str = target_hn_name
        self.edge_type: EdgeType = edge_type

    def to_json(self) -> Dict[str, Any]:
        return {
            "sourceHnName": self.source_hn_name,
            "targetHnName": self.target_hn_name,
            "edgeType": "Then" if self.edge_type == EdgeType.ThenEdge else (
                "Link" if self.edge_type == EdgeType.LinkEdge else None)
        }

    def __str__(self):
        return (f"HiddenEdge("
                f"source = {self.source_hn_name}, "
                f"target = {self.target_hn_name}, "
                f"type = {self.edge_type.name})"
                )

    def __repr__(self):
        return self.__str__()
