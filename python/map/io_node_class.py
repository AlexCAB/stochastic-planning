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
| created: 2025-01-29 ||||||||||"""

from typing import Any, List, TYPE_CHECKING, Dict, Tuple

from map.io_variable_class import IoVariable

if TYPE_CHECKING:
    from map.hidden_node_class import ConcreteHiddenNode


class IoNode:
    def __init__(
            self,
            name: str,
            variable: IoVariable,
            neo4j_id: str | None):

        assert name is not None, "Name should be defined"
        assert variable is not None, "Variable should be defined"

        self.name: str = name
        self.variable: IoVariable = variable
        self.neo4j_id: str = neo4j_id

        self.hidden_nodes: Dict['ConcreteHiddenNode', int] = {} # hidden nodes with value index

    def add_hidden_node(self, hidden_node: 'ConcreteHiddenNode', value_index: int):
        self.hidden_nodes[hidden_node] = value_index

    def remove_hidden_node(self, hidden_node: 'ConcreteHiddenNode'):
        assert hidden_node in self.hidden_nodes, \
            f"Hidden node {hidden_node} is not in the list of hidden nodes, for node {self}"

        self.hidden_nodes.pop(hidden_node)

    def __str__(self):
        return f"{self.name}: {self.variable}, neo4j_id = {self.neo4j_id}"

class InputNode(IoNode):
    def __init__(self, name: str, variable: IoVariable, neo4j_id: str | None  = None):
        super().__init__(name, variable, neo4j_id)


class OutputNode(IoNode):
    def __init__(self, name: str, variable: IoVariable, neo4j_id: str | None  = None):
        super().__init__(name, variable, neo4j_id)
