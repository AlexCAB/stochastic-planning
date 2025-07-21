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
            variable: IoVariable):

        assert name is not None, "Name should be defined"
        assert variable is not None, "Variable should be defined"

        self.name: str = name
        self.variable: IoVariable = variable

        self.hidden_nodes: Dict['ConcreteHiddenNode', int] = {}  # hidden nodes with value index

    def add_hidden_node(self, hidden_node: 'ConcreteHiddenNode', value_index: int):
        self.hidden_nodes[hidden_node] = value_index

    def remove_hidden_node(self, hidden_node: 'ConcreteHiddenNode'):
        assert hidden_node in self.hidden_nodes, \
            f"Hidden node {hidden_node} is not in the list of hidden nodes, for node {self}"

        self.hidden_nodes.pop(hidden_node)

    def __str__(self):
        return f"{self.name}: {self.variable}"

    def __repr__(self):
        return self.__str__()

    def to_properties(self) -> Dict[str, Any]:
        match self:
            case t if isinstance(t, InputNode):
                node_type = 'input'
            case t if isinstance(t, OutputNode):
                node_type = 'output'
            case t:
                raise ValueError(f"Unknown node type, should be InputNode or OutputNode, but got {t}")
        return {
            'name': self.name,
            'type': node_type,
            'variable': self.variable.to_properties()
        }


class InputNode(IoNode):
    def __init__(self, name: str, variable: IoVariable):
        super().__init__(name, variable)


class OutputNode(IoNode):
    def __init__(self, name: str, variable: IoVariable):
        super().__init__(name, variable)
