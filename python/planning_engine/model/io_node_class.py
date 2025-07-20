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
| created: 2025-07-19 ||||||||||"""

from typing import List, Dict, Any


class IoNode:
    def __init__(self, name: str, node_type: str):
        assert name, "Name should be defined"
        assert type is not None, "Type should be defined"

        self.name: str = name
        self.node_type: str = node_type

    def value_for_index(self, index: int) -> bool | int | float | str:
        raise NotImplementedError("Abstract method")

    def index_for_value(self, value: bool | int | float | str) -> int:
        raise NotImplementedError("Abstract method")

    def to_json(self) -> Dict[str, Any]:
        raise NotImplementedError("Abstract method")

    def __repr__(self):
        return self.__str__()


class BoolIoNode(IoNode):
    def __init__(self, name: str, acceptable_values: List[bool]):
        super().__init__(name, node_type="BooleanIoNode")
        assert isinstance(acceptable_values, list), "Value range should be list for bool type"
        assert all(isinstance(v, bool) for v in acceptable_values), "Values should be bool for bool type"

        self.acceptable_values: List[bool] = acceptable_values

    def value_for_index(self, index: int) -> bool:
        assert index == 0 or index == 1, f"Invalid index {index} for type bool, should be 0 or 1"
        value = True if index == 1 else False
        assert value in self.acceptable_values, f"Bool value {value} is not in the domain {self.acceptable_values}"
        return True if index == 1 else False

    def index_for_value(self, value: bool) -> int:
        assert value in self.acceptable_values, f"Bool value {value} is not in the domain {self.acceptable_values}"
        return 1 if value else 0

    def to_json(self) -> Dict[str, Any]:
        return {
            "type": self.node_type,
            "data": {
                "name": self.name,
                "acceptableValues": self.acceptable_values
            }
        }

    def __str__(self):
        return f"BoolIoNode(acceptable_values = {self.acceptable_values})"


class IntIoNode(IoNode):
    def __init__(self, name: str, min: int, max: int):
        super().__init__(name, node_type="IntIoNode")
        assert isinstance(min, int) and isinstance(max, int), "Min and max should be integers"
        assert min < max, "Min should be less than max"

        self.min: int = min
        self.max: int = max

    def value_for_index(self, index: int) -> int:
        assert \
            self.min <= index <= self.max, \
            f"Invalid index {index} for type int, should be {self.min} <= index <= {self.max}"

        return index

    def index_for_value(self, value: int) -> int:
        assert \
            self.min <= value <= self.max, \
            f"Invalid value {value} for type int, should be {self.min} <= value <= {self.max}"

        return value

    def to_json(self) -> Dict[str, Any]:
        return {
            "type": self.node_type,
            "data": {
                "name": self.name,
                "min": self.min,
                "max": self.max
            }
        }

    def __str__(self):
        return f"IntIoNode(min = {self.min}, max = {self.max})"


class FloatIoNode(IoNode):
    def __init__(self, name: str, min: float, max: float):
        super().__init__(name, node_type="FloatIoNode")
        assert isinstance(min, float) and isinstance(max, float), "Min and max should be floats"
        assert min < max, "Min should be less than max"

        self.min: float = min
        self.max: float = max

    def value_for_index(self, index: int) -> float:
        value = float(index) / 10000.0

        assert \
            self.min <= value <= self.max, \
            f"Invalid value {value} of index {index} for type float, should be {self.min} <= value <= {self.max}"

        return value

    def index_for_value(self, value: float) -> int:
        assert \
            self.min <= value <= self.max, \
            f"Invalid value {value} for type float, should be {self.min} <= value <= {self.max}"

        return int(value * 10000)

    def to_json(self) -> Dict[str, Any]:
        return {
            "type": self.node_type,
            "data": {
                "name": self.name,
                "min": self.min,
                "max": self.max
            }
        }


    def __str__(self):
        return f"FloatIoNode(min = {self.min}, max = {self.max})"


class ListStrIoNode(IoNode):
    def __init__(self, name: str, elements: List[str]):
        super().__init__(name, node_type="ListStrIoNode")
        assert isinstance(elements, list), "Value range should be list for str type"
        assert all(isinstance(v, str) for v in elements), "Values should be str for str type"

        self.elements: List[str] = elements

    def value_for_index(self, index: int) -> str:
        assert \
            0 <= index < len(self.elements), \
            f"Invalid index {index} for type list, should be 0 <= index < {len(self.elements)}"

        return self.elements[index]

    def index_for_value(self, value: str) -> int:
        assert \
            value in self.elements, \
            f"Invalid value {value} for type list, should be in {self.elements}"

        return self.elements.index(value)

    def to_json(self) -> Dict[str, Any]:
        return {
            "type": self.node_type,
            "data": {
                "name": self.name,
                "elements": self.elements
            }
        }

    def __str__(self):
        return f"ListStrIoNode(elements = {self.elements})"
