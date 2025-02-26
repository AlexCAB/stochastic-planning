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
| created: 2025-02-27 ||||||||||"""

from bs4 import Tag
from enum import Enum


class ParsedEdgeType(Enum):
    LinkEdge = 1
    ThenEdge = 2
    SupportEdge = 3


class ParsedEdge:
    def __init__(self, edge_elem: Tag):
        self.source_id: str = edge_elem.attrs['source']
        self.target_id: str = edge_elem.attrs['target']

        match edge_elem.find('y:linestyle').attrs['color']:
            case "#FF0000":
                self.edge_type = ParsedEdgeType.LinkEdge
            case "#00FF00":
                self.edge_type = ParsedEdgeType.ThenEdge
            case "#888888":
                self.edge_type = ParsedEdgeType.SupportEdge
            case _:
                raise ValueError(f"Invalid color for edge {edge_elem}")

        assert self.edge_type is not None, "Edge type should be defined"
        assert self.source_id, "Source id should be defined"
        assert self.target_id, "Target id should be defined"

    def __str__(self):
        return f"Edge: {self.source_id} - {self.edge_type} -> {self.target_id}"
