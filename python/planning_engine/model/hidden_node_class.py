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

from typing import Dict, Any, Optional


class HiddenNode:
    def __init__(self, name: str, description: Optional[str]):
        assert name, "Name should be defined"

        self.name: str = name
        self.description: str = description

    def to_json(self) -> Dict[str, Any]:
        raise NotImplementedError("Abstract method")

    def __repr__(self):
        return self.__str__()


class ConcreteNode(HiddenNode):
    def __init__(self, name: str, description: Optional[str], io_node_name: str, value: Any):
        super().__init__(name, description)

        assert io_node_name, "IO node name should be defined"
        assert value is not None, "Value should be defined"

        self.io_node_name: str = io_node_name
        self.value: Any = value

    def to_json(self) -> Dict[str, Any]:
        return {
            "type": "ConcreteNode",
            "data": {
                "name": self.name,
                "description": self.description,
                "ioNodeName": self.io_node_name,
                "value": self.value
            }
        }

    def __eq__(self, other) -> bool:
        return isinstance(other, ConcreteNode) \
            and self.name == other.name \
            and self.io_node_name == other.io_node_name \
            and self.value == other.value

    def __str__(self):
        return f"ConcreteNode(name = {self.name}, description = {self.description}, ioNodeName = {self.io_node_name}, value = {self.value})"


class AbstractNode(HiddenNode):
    def __init__(self, name: str, description: Optional[str]):
        super().__init__(name, description)

    def to_json(self) -> Dict[str, Any]:
        return {
            "type": "AbstractNode",
            "data": {
                "name": self.name,
                "description": self.description
            }
        }

    def __eq__(self, other) -> bool:
        return isinstance(other, AbstractNode) \
            and self.name == other.name

    def __str__(self):
        return f"AbstractNode(name = {self.name}, description = {self.description})"
