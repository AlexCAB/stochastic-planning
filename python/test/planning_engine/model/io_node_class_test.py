import unittest

from planning_engine.model.io_node_class import BoolIoNode, IntIoNode, FloatIoNode, ListStrIoNode


class TestIoNode(unittest.TestCase):
    def test_creates_bool_io_node_with_valid_values(self):
        node = BoolIoNode(name="boolNode", acceptable_values=[True, False])
        self.assertEqual(node.name, "boolNode")
        self.assertEqual(node.acceptable_values, [True, False])

    def test_creates_int_io_node_with_valid_range(self):
        node = IntIoNode(name="intNode", min=0, max=10)
        self.assertEqual(node.name, "intNode")
        self.assertEqual(node.min, 0)
        self.assertEqual(node.max, 10)

    def test_raises_error_for_invalid_int_range(self):
        with self.assertRaises(AssertionError):
            IntIoNode(name="intNode", min=10, max=0)

    def test_creates_float_io_node_with_valid_range(self):
        node = FloatIoNode(name="floatNode", min=0.0, max=1.0)
        self.assertEqual(node.name, "floatNode")
        self.assertEqual(node.min, 0.0)
        self.assertEqual(node.max, 1.0)

    def test_raises_error_for_invalid_float_range(self):
        with self.assertRaises(AssertionError):
            FloatIoNode(name="floatNode", min=1.0, max=0.0)

    def test_creates_list_str_io_node_with_valid_elements(self):
        node = ListStrIoNode(name="listNode", elements=["a", "b", "c"])
        self.assertEqual(node.name, "listNode")
        self.assertEqual(node.elements, ["a", "b", "c"])

    def test_raises_error_for_invalid_list_elements(self):
        with self.assertRaises(AssertionError):
            ListStrIoNode(name="listNode", elements=[1, 2, 3])


class TestBoolIoNode(unittest.TestCase):
    def __init__(self, *args, **kwargs):
        super(TestBoolIoNode, self).__init__(*args, **kwargs)
        self.node = BoolIoNode(name="testBoolNode", acceptable_values=[True, False])

    def test_value_for_index(self):
        self.assertTrue(self.node.value_for_index(1))
        self.assertFalse(self.node.value_for_index(0))

    def test_index_for_value(self):
        self.assertEqual(self.node.index_for_value(True), 1)
        self.assertEqual(self.node.index_for_value(False), 0)

    def test_to_json(self):
        expected_json = {
            "type": "BooleanIoNode",
            "data": {
                "name": "testBoolNode",
                "acceptableValues": [True, False]
            }
        }
        self.assertEqual(self.node.to_json(), expected_json)


class TestIntIoNode(unittest.TestCase):
    def __init__(self, *args, **kwargs):
        super(TestIntIoNode, self).__init__(*args, **kwargs)
        self.node = IntIoNode(name="testIntNode", min=0, max=10)

    def test_value_for_index(self):
        self.assertEqual(self.node.value_for_index(5), 5)

    def test_index_for_value(self):
        self.assertEqual(self.node.index_for_value(5), 5)

    def test_to_json(self):
        expected_json = {
            "type": "IntIoNode",
            "data": {
                "name": "testIntNode",
                "min": 0,
                "max": 10
            }
        }
        self.assertEqual(self.node.to_json(), expected_json)

class TestFloatIoNode(unittest.TestCase):
    def __init__(self, *args, **kwargs):
        super(TestFloatIoNode, self).__init__(*args, **kwargs)
        self.node = FloatIoNode(name="testFloatNode", min=0.0, max=1.0)

    def test_value_for_index(self):
        self.assertAlmostEqual(self.node.value_for_index(5000), 0.5)

    def test_index_for_value(self):
        self.assertEqual(self.node.index_for_value(0.5), 5000)

    def test_to_json(self):
        expected_json = {
            "type": "FloatIoNode",
            "data": {
                "name": "testFloatNode",
                "min": 0.0,
                "max": 1.0
            }
        }
        self.assertEqual(self.node.to_json(), expected_json)


class TestListStrIoNode(unittest.TestCase):
    def __init__(self, *args, **kwargs):
        super(TestListStrIoNode, self).__init__(*args, **kwargs)
        self.node = ListStrIoNode(name="testListStrNode", elements=["a", "b", "c"])

    def test_value_for_index(self):
        self.assertEqual(self.node.value_for_index(1), "b")

    def test_index_for_value(self):
        self.assertEqual(self.node.index_for_value("b"), 1)

    def test_to_json(self):
        expected_json = {
            "type": "ListStrIoNode",
            "data": {
                "name": "testListStrNode",
                "elements": ["a", "b", "c"]
            }
        }
        self.assertEqual(self.node.to_json(), expected_json)


if __name__ == '__main__':
    unittest.main()
