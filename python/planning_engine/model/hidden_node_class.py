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

from typing import Dict, Any


class HiddenNode:
    def __init__(self, name: str):
        assert name, "Name should be defined"

        self.name: str = name

    def to_json(self) -> Dict[str, Any]:
        raise NotImplementedError("Abstract method")

    def __repr__(self):
        return self.__str__()


class ConcreteNode(HiddenNode):
    def __init__(self, name: str, io_node_name: str, value: Any):
        super().__init__(name)

        assert io_node_name, "IO node name should be defined"
        assert value is not None, "Value should be defined"

        self.io_node_name: str = io_node_name
        self.value: Any = value

    def to_json(self) -> Dict[str, Any]:
        return {
            "type": "ConcreteNode",
            "data": {
                "name": self.name,
                "ioNodeName": self.io_node_name,
                "value": self.value
            }
        }

    def __str__(self):
        return f"ConcreteNode(name={self.name}, ioNodeName={self.io_node_name}, value={self.value})"


class AbstractNode(HiddenNode):
    def __init__(self, name: str):
        super().__init__(name)

    def to_json(self) -> Dict[str, Any]:
        return {
            "type": "AbstractNode",
            "data": {
                "name": self.name
            }
        }

    def __str__(self):
        return f"AbstractNode(name={self.name})"
