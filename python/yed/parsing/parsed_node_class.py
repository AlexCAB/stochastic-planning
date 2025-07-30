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
| created: 2025-02-19 ||||||||||"""

from typing import List, Any, Tuple


class ParsedNode:
    def __init__(self, id: str, label: str):
        split_label = label.split(":")

        self.id: str = id
        self.node_type: str = split_label[0].strip()

        assert self.id, f"Node ID is empty or None, label = {label}"
        assert self.node_type, f"Node type is empty or None, label = {label}"
        assert self.node_type in ["G", "S", "I", "O", "C", "A"], f"Unknown node type: {self.node_type}, label = {label}"

        self.name: str = ""
        self.description: str | None = None
        self.probability_count: int | None = None
        self.utility: float | None = None
        self.value_type: type | None = None
        self.value_range: List[bool] | Tuple[int, int] | Tuple[float, float] | List[Any] | None = None
        self.variable_name: str | None = None
        self.value: str | None = None

        match split_label[0]:
            case "G":
                self.name = split_label[1].strip()
                self.description = ":".join(split_label[2:])

            case "S":
                self.name = split_label[1].strip()
                self.probability_count = int(split_label[2].strip())
                self.utility = float(split_label[3].strip())
                self.description = ":".join(split_label[4:])

                assert \
                    self.probability_count is not None and self.probability_count > 0, \
                    f"Probability count is None or <= 0, label = {label}"

                assert self.utility is not None, f"Utility is None, label = {label}"

            case "I" | "O":
                self.name = split_label[1].strip()
                split_value_range = [v.strip() for v in split_label[3].split(";")]

                match split_label[2]:
                    case "bool":
                        self.value_type = bool
                        self.value_range = [bool(v) for v in split_value_range]

                        assert \
                            0 < len(self.value_range) <= 2, \
                            f"Invalid value range: {split_label[3]}, for type `bool` expected 1 or 2 values, " \
                            "label = {label}"

                    case "int":
                        self.value_type = int
                        value_range = [int(v) for v in split_value_range]

                        assert \
                            len(value_range) == 2, \
                            f"Invalid value range: {split_label[3]}, for type `int` expected 2 values (min, max), " \
                            "label = {label}"

                        self.value_range = (value_range[0], value_range[1])

                    case "float":
                        self.value_type = float
                        value_range: List[float] = [float(v) for v in split_value_range]

                        assert \
                            len(value_range) == 2, \
                            f"Invalid value range: {split_label[3]}, for type `float` expected 2 values (min, max), " \
                            "label = {label}"

                        self.value_range = (value_range[0], value_range[1])

                    case "list":
                        self.value_type = list
                        self.value_range = split_value_range

                        assert self.value_range, f"List of values is empty, label = {label}"

                    case _:
                        raise ValueError(
                            f"Unknown value type: {split_label[2]}, expected one of: bool, int, float or list, "
                            f"label = {label}")

                self.description = ":".join(split_label[4:])

            case "C":
                self.variable_name = split_label[1].strip()
                self.value = split_label[2].strip()
                self.name = split_label[3].strip()
                self.description = ":".join(split_label[4:])

                assert \
                    self.variable_name != "" and self.variable_name is not None, \
                    f"Variable name is empty or None, label = {label}"

                assert self.value != "" and self.value is not None, f"Value is empty or None, label = {label}"

            case "A":
                self.name = split_label[1].strip()
                self.description = ":".join(split_label[2:])

            case _:
                raise ValueError(f"Unknown node type: {split_label[0]}, expected one of: G, S, I, O, C, A")

        assert self.name != "" and self.name is not None, f"Node name is empty or None, label = {label}"

        self.description = self.description if self.description != "" else None

    def is_graph_node(self) -> bool:
        return self.node_type == "G"

    def is_sample_node(self) -> bool:
        return self.node_type == "S"

    def is_input_node(self) -> bool:
        return self.node_type == "I"

    def is_output_node(self) -> bool:
        return self.node_type == "O"

    def is_concrete_node(self) -> bool:
        return self.node_type == "C"

    def is_abstract_node(self) -> bool:
        return self.node_type == "A"

    def __repr__(self):
        return self.__str__()

    def __str__(self):
        return f"Node(id = {self.id}, node_type = {self.node_type}, name = {self.name}, " \
               f"description = {self.description}, probability_count = {self.probability_count}, " \
               f"utility = {self.utility}, value_type = {self.value_type}, value_range = {self.value_range}, " \
               f"variable_name = {self.variable_name}, value = {self.value})"
