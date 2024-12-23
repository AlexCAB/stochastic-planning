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
| created:  2024-12-23 |||||||||"""

from typing import Any, List


class Value:
    pass


class TimeValue(Value):
    def __init__(self, time: int):
        self.time: int = time


class BoolValue(Value):
    def __init__(self, value: bool):
        self.value: bool = value


class IntValue(Value):
    def __init__(self, value: int):
        self.value: int = value


class FloatValue(Value):
    def __init__(self, value: float):
        self.value: float = value


class EnumValue(Value):
    def __init__(self, value: str):
        self.value: str = value


class Variable:
    def __init__(self):
        pass

    def value_for_index(self, index: int) -> Any:
        raise NotImplementedError()


class TimeVariable(Variable):
    def __init__(self):
        super().__init__()

    def value_for_index(self, index: int) -> TimeValue:
        assert(index >= 0, f"Invalid index {index}, should be non-negative")
        return TimeValue(index)


class BoolVariable(Variable):
    def __init__(self):
        super().__init__()

    def value_for_index(self, index: int) -> BoolValue:
        match index:
            case 0:
                return BoolValue(False)
            case 1:
                return BoolValue(True)
            case _:
                raise ValueError(f"Invalid index: {index}, should be 0 or 1")


class IntVariable(Variable):
    def __init__(self):
        super().__init__()

    def value_for_index(self, index: int) -> IntValue:
        return IntValue(index)


class FloatVariable(Variable):
    def __init__(self):
        super().__init__()

    def value_for_index(self, index: int) -> FloatValue:
        return FloatValue(index / 10000.0)


class EnumVariable(Variable):
    def __init__(self, options: List[str]):
        super().__init__()
        self.options: List[str] = options


    def value_for_index(self, index: int) -> EnumValue:
        assert(
            0 <= index < len(self.options),
            f"Invalid index: {index}, for options list {self.options}")
        return EnumValue(self.options[index])
