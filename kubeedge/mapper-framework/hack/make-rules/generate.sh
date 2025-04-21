#!/usr/bin/env bash

set -o errexit
set -o nounset
set -o pipefail

CURR_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd -P)"
ROOT_DIR="${CURR_DIR}"
MAPPER_DIR="$(cd "$(dirname "$ROOT_DIR")" && pwd -P)"

function entry() {
  # copy template
  read -p "Please input the mapper name (like 'Bluetooth', 'BLE'): " -r mapperName
  if [[ -z "${mapperName}" ]]; then
    echo "the mapper name is required"
    exit 1
  fi
  mapperNameLowercase=$(echo -n "${mapperName}" | tr '[:upper:]' '[:lower:]')
  mapperPath="${MAPPER_DIR}/${mapperNameLowercase}"
  if [[ -d "${mapperPath}" ]]; then
    echo "the directory is existed"
    exit 1
  fi
  cp -r "${ROOT_DIR}/_template/mapper" "${mapperPath}"
  cp -r "${ROOT_DIR}/pkg" "${mapperPath}"
  cp "${ROOT_DIR}/go.mod" "${mapperPath}"
  cp "${ROOT_DIR}/go.sum" "${mapperPath}"

  # Capitalize the first letter
  mapperVar=$(echo "${mapperName}" | awk '{print toupper(substr($0,1,1)) substr($0,2)}')

  # Replace Template with mapper name
  find "${mapperPath}" -type f -exec sed -i "s/Template/${mapperVar}/g" {} +

  # Replace kubeedge/<MapperVar> with kubeedge/<mapperNameLowercase>
  find "${mapperPath}" -type f -exec sed -i "s/kubeedge\/${mapperVar}/kubeedge\/${mapperNameLowercase}/g" {} +

  empty_file_path="${MAPPER_DIR}/.empty"
  if [ -f "$empty_file_path" ]; then
      rm "$empty_file_path"
  fi
  echo "You can find your customized mapper in mappers"
}

entry "$@"