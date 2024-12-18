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

from typing import Any, Self

from map.graph import *


class ValueNode(Node):
    """
    A class to represent a value node in outcome graph.

    """

    def __init__(self, variable_type: VariableType, variable_id: str | None, value: Any):
        """
        Constructs value node.

        Parameters
        ----------
        variable_type : VariableType
            The type of the variable [IN, MID, OUT].
        variable_id : str or None
            The identifier of the variable (form Neo4j).
        value : Any
            The value of the node.
        """

        super().__init__(variable_type)

        self.variable_id: Any = variable_id
        self.value: Any = value


class RelationEdge(Edge):
    """
    A class to represent a relation edge in outcome graph.

    """

    def __init__(self, relation_type: RelationType, source_value: ValueNode, target_value: ValueNode):
        """
        Constructs relation edge.

        Parameters
        ----------
        relation_type : RelationType
            The type of the relation [LINK, THEN].
        source_value : ValueNode
            The source value node of the relation.
        target_value : ValueNode
            The target value node of the relation.
        """

        super().__init__(relation_type)

        self.source_value = source_value
        self.target_value = target_value


class OutcomeGraph:
    """
    A class to represent an outcome graph.

    """

    def __init__(self, value_nodes: list[ValueNode], relation_edges: list[RelationEdge], probability_count: int):
        """
        Constructs an outcome graph.

        Parameters
        ----------
        value_nodes : list[ValueNode]
            A list of value nodes in the outcome graph.
        relation_edges : list[RelationEdge]
            A list of relation edges in the outcome graph.
        probability_count : int
            The probability count of outcome graph.
        """

        self.value_nodes: list[ValueNode] = value_nodes
        self.relation_edges: list[RelationEdge] = relation_edges
        self.probability_count = probability_count
