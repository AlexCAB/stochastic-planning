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
| created: 2025-07-16 ||||||||||"""

import logging
from typing import Dict, Any, List

import requests
from requests import Response

from planning_engine.model.added_sample_class import AddedSample
from planning_engine.model.map_definition_class import MapDefinition
from planning_engine.model.map_info_class import MapInfo
from planning_engine.model.pe_client_conf_class import PeClientConf
from planning_engine.model.sample_class import Sample


class PeClient:
    PATH_HEALTH = "/pe/v1/maintenance/__health"
    PATH_EXIT = "/pe/v1/maintenance/__exit"
    PATH_RESET = "/pe/v1/map/reset"
    PATH_INIT = "/pe/v1/map/init"
    PATH_LOAD = "/pe/v1/map/load"
    PATH_SAMPLES = "/pe/v1/map/samples"

    def __init__(self, config: PeClientConf):
        self.logger = logging.getLogger(self.__class__.__name__)
        self.base_url: str = config.url

        pe_status = self._run_get(PeClient.PATH_HEALTH)
        self.logger.info(f"Connecting to planning engine at {self.base_url}, status: {pe_status}")

    def _check_response(self, response: requests.Response, methode: str, request_path: str) -> Response:
        if response.status_code != 200:
            raise ConnectionError(f"Failed to run {methode} {self.base_url}{request_path}. "
                                  f"Status code: {response.status_code}, Response: {response.text}")
        return response

    def _parse_response(self, response: requests.Response, request_path: str) -> Dict[str, Any]:
        json_data = response.json()
        assert isinstance(json_data, Dict), \
            f"Expected JSON response, got {type(json_data)}, for path  {self.base_url}{request_path}"
        return json_data

    def _run_get(self, request_path: str) -> Dict[str, Any]:
        return self._parse_response(
            self._check_response(requests.get(self.base_url + request_path), "GET", request_path),
            request_path)

    def _run_post(self, request_path: str, data: Dict) -> Dict[str, Any]:
        return self._parse_response(
            self._check_response(requests.post(self.base_url + request_path, json=data), "POST", request_path),
            request_path)

    def kill_planning_engine(self):
        response = requests.post(self.base_url + PeClient.PATH_EXIT)
        self._check_response(response, "POST", PeClient.PATH_EXIT)
        self.logger.info(f"Exit signal sent to planning engine, response: {response}")

    def reset_map(self):
        response = requests.post(self.base_url + PeClient.PATH_RESET)
        self._check_response(response, "POST", PeClient.PATH_RESET)
        self.logger.info(f"Map reset, response: {response}")

    def init_map(self, definition: MapDefinition) -> MapInfo:
        response = self._run_post(PeClient.PATH_INIT, definition.to_json())
        self.logger.info(f"Map initialized, definition: {definition}, response: {response}")
        return MapInfo.from_json(response)

    def load_map(self, db_name: str) -> MapInfo:
        response = self._run_post(PeClient.PATH_LOAD, data={"dbName": db_name})
        self.logger.info(f"Map loaded, from db_name: {db_name}, response: {response}")
        return MapInfo.from_json(response)

    def add_samples(self, samples: List[Sample]) -> List[AddedSample]:
        response = self._run_post(PeClient.PATH_SAMPLES, data={"samples": [s.to_json() for s in samples]})
        assert 'addedSamples' in response, "Samples should be defined in JSON response"
        self.logger.info(f"Added samples: {samples}, response: {response}")
        return [AddedSample.from_json(j) for j in response['addedSamples']]
