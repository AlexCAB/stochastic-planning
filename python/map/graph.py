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
| created:  2024-12-18 |||||||||"""

from enum import Enum


class VariableType(Enum):
    IN = 1
    MID = 2
    OUT = 3


class RelationType(Enum):
    LINK = 1
    THEN = 2


class Node:
    def __init__(self, type_: VariableType):
        self.variable_type: VariableType = type_


class Edge:
    def __init__(self, type_: RelationType):
        self.relation_type: RelationType = type_
