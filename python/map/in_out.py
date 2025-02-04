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

from typing import Any, List, TYPE_CHECKING, Dict

if TYPE_CHECKING:
    from map.hidden import ConcreteHiddenNode


class IoNode:
    def __init__(self, _type: type, name: str, neo4j_id: str, list_values: List[Any] = None):
        assert _type is not None, "Type should be defined"
        assert \
            _type is bool or _type is int or _type is float or _type is list, \
            f"Type should be bool, int, float or list, but got {str(_type)}. Example of usage: IoNode(int)"

        assert name is not None, "Name should be defined"
        assert neo4j_id is not None, "Neo4j id should be defined"
        assert list_values is not None or _type is not list, "List values should be defined for list type"

        self._type: type = _type
        self.name: str = name
        self.neo4j_id: str = neo4j_id
        self.list_values: List[Any] = list_values

        self.hidden_nodes: Dict['ConcreteHiddenNode', int] = {} # hidden nodes with value index

    def add_hidden_node(self, hidden_node: 'ConcreteHiddenNode', value_index: int):
        self.hidden_nodes[hidden_node] = value_index

    def remove_hidden_node(self, hidden_node: 'ConcreteHiddenNode'):
        assert hidden_node in self.hidden_nodes, \
            f"Hidden node {hidden_node} is not in the list of hidden nodes, for node {self}"

        self.hidden_nodes.pop(hidden_node)

    def value_for_index(self, index: int):
        match self._type:
            case t if t is bool:
                assert index == 0 or index == 1, f"Invalid index {index} for type {str(self._type)}, should be 0 or 1"
                return True if index == 1 else False
            case t if t is int:
                return index
            case t if t is float:
                return float(index) / 10000.0
            case t if t is list:
                assert \
                    0 <= index < len(self.list_values), \
                    f"Invalid index {index} for type {str(self._type)}, should be 0 <= index < {len(self.list_values)}"
                return self.list_values[index]
            case _:
                raise NotImplementedError(f"Type {str(self._type)} is not supported")

    def index_for_value(self, value: Any):
        match self._type:
            case t if t is bool:
                assert \
                    value is True or value is False, f"Invalid value {value} for type {str(self._type)}, \
                    should be True or False"
                return 1 if value is True else 0
            case t if t is int:
                return value
            case t if t is float:
                return int(value * 10000)
            case t if t is list:
                assert \
                    value in self.list_values, \
                    f"Invalid value {value} for type {str(self._type)}, should be in {self.list_values}"
                return self.list_values.index(value)
            case _:
                raise NotImplementedError(f"Type {str(self._type)} is not supported")


class InputNode(IoNode):
    def __init__(self, _type: type, name: str, neo4j_id: str, list_values: List[Any] = None):
        super().__init__(_type, name, neo4j_id, list_values)


class OutputNode(IoNode):
    def __init__(self, _type: type, name: str, neo4j_id: str, list_values: List[Any] = None):
        super().__init__(_type, name, neo4j_id, list_values)




