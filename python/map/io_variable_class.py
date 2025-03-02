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
| created: 2025-02-18 ||||||||||"""

from typing import List, Any, Tuple, Dict


class IoVariable:
    @staticmethod
    def create_variable(
            _type: type,
            domain: List[bool] | Tuple[int, int] | Tuple[float, float] | List[Any]) -> 'IoVariable':

        match _type:
            case t if t is bool:
                return BoolVariable(domain)
            case t if t is int:
                return IntVariable(domain)
            case t if t is float:
                return FloatVariable(domain)
            case t if t is list:
                return ListVariable(domain)
            case _:
                raise ValueError(f"Invalid type {_type} for IoVariable")

    def value_for_index(self, index: int) -> bool | int | float | Any:
        raise NotImplementedError("Abstract method")

    def index_for_value(self, value: bool | int | float | Any) -> int:
        raise NotImplementedError("Abstract method")

    def to_properties(self) -> Dict[str, Any]:
        raise NotImplementedError("Abstract method")



class BoolVariable(IoVariable):
    def __init__(self, domain: List[bool]):
        assert isinstance(domain, list), "Value range should be list for bool type"
        assert 0 < len(set(domain)) <= 2, "Value range should have 1 or 2 different values for bool type"
        assert all(isinstance(v, bool) for v in domain), "Values should be bool for bool type"

        self.domain: List[bool] = domain

    def value_for_index(self, index: int) -> bool:
        assert index == 0 or index == 1, f"Invalid index {index} for type bool, should be 0 or 1"
        value = True if index == 1 else False
        assert value in self.domain, f"Bool value {value} is not in the domain {self.domain}"
        return True if index == 1 else False

    def index_for_value(self, value: bool) -> int:
        assert value in self.domain, f"Bool value {value} is not in the domain {self.domain}"
        return 1 if value else 0

    def __str__(self):
        return f"bool{self.domain}"

    def to_properties(self) -> Dict[str, Any]:
        return {
            'type': 'bool',
            'domain': ['t' if n else 'f' for n in self.domain]
        }


class IntVariable(IoVariable):
    def __init__(self, domain: Tuple[int, int]):
        assert isinstance(domain, tuple), "Value range should be tuple for int type"
        assert len(domain) == 2, "Value range should have 2 values for int type"
        assert all(isinstance(value, int) for value in domain), "Values should be int for int type"
        assert domain[0] <= domain[1], "First value should be <= than the second value for int type"

        self.domain: Tuple[int, int] = domain

    def value_for_index(self, index: int) -> int:
        assert \
            self.domain[0] <= index <= self.domain[1], \
            f"Invalid index {index} for type int, should be {self.domain[0]} <= index <= {self.domain[1]}"

        return index

    def index_for_value(self, value: int) -> int:
        assert \
            self.domain[0] <= value <= self.domain[1], \
            f"Invalid value {value} for type int, should be {self.domain[0]} <= value <= {self.domain[1]}"

        return value

    def __str__(self):
        return f"int{self.domain}"

    def to_properties(self) -> Dict[str, Any]:
        return {
            'type': 'int',
            'min': self.domain[0],
            'max': self.domain[1]
        }


class FloatVariable(IoVariable):
    def __init__(self, domain: Tuple[float, float]):
        assert isinstance(domain, tuple), "Value range should be tuple for float type"
        assert len(domain) == 2, "Value range should have 2 values for float type"
        assert all(isinstance(value, float) for value in domain), "Values should be float for float type"
        assert domain[0] <= domain[1], "First value should be <= than the second value for float type"

        self.domain: Tuple[float, float] = domain

    def value_for_index(self, index: int) -> float:
        value = float(index) / 10000.0

        assert \
            self.domain[0] <= value <= self.domain[1], \
            f"Invalid value {value} of index {index} for type float, should " \
            "be {self.domain[0]} <= value <= {self.domain[1]}"

        return value

    def index_for_value(self, value: float) -> int:
        assert \
            self.domain[0] <= value <= self.domain[1], \
            f"Invalid value {value} for type float, should be {self.domain[0]} <= value <= {self.domain[1]}"

        return int(value * 10000)

    def __str__(self):
        return f"float{self.domain}"

    def to_properties(self) -> Dict[str, Any]:
        return {
            'type': 'float',
            'min': self.domain[0],
            'max': self.domain[1]
        }


class ListVariable(IoVariable):
    def __init__(self, domain: List[Any]):
        assert isinstance(domain, list), "Value range should be list for list type"
        assert len(domain) > 0, "Value range should have at least one value for list type"

        self.domain: List[Any] = domain

    def value_for_index(self, index: int) -> Any:
        assert \
            0 <= index < len(self.domain), \
            f"Invalid index {index} for type list, should be 0 <= index < {len(self.domain)}"

        return self.domain[index]

    def index_for_value(self, value: Any) -> int:
        assert \
            value in self.domain, \
            f"Invalid value {value} for type list, should be in {self.domain}"

        return self.domain.index(value)

    def __str__(self):
        return f"list{self.domain}"

    def __repr__(self):
        return self.__str__()

    def to_properties(self) -> Dict[str, Any]:
        return {
            'type': 'list',
            'domain': self.domain
        }
