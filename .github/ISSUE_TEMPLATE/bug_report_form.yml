name: 🐞 Bug Report
description: 버그 보고 및 해결을 위한 이슈
title: "[BUG] "
labels: ["bug"]
body:
  - type: markdown
    attributes:
      value: |
        버그의 발생 원인을 보고하고 해결하기 위한 이슈입니다.

  - type: textarea
    id: description
    attributes:
      label: 🐞 Description
      description: 발생한 버그에 대해 간략하게 설명해주세요.
      placeholder: 로그인 화면에서 버튼이 눌리지 않습니다.
    validations:
      required: true

  - type: checkboxes
    id: modules
    attributes:
      label: 🚨 Affected Modules
      description: 버그가 발생한 모듈을 선택해주세요.
      options:
        - label: Shared
        - label: Server
        - label: Android
        - label: iOS
        - label: 기타
    validations:
      required: true

  - type: textarea
    id: reproduction
    attributes:
      label: 👣 Reproduction Steps
      description: 버그가 발생한 상황(혹은 재현을 위한 순서)을 적어주세요.
      placeholder: |
        1. Go to '...'
        2. Click on '...'
        3. See error
    validations:
      required: true

  - type: textarea
    id: expected
    attributes:
      label: 🤔 Expected Behavior
      description: 원래 의도했던 동작은 무엇인지 적어주세요.
    validations:
      required: true

  - type: input
    id: environment
    attributes:
      label: 📱 Environment
      description: 기기명, OS 버전 등을 적어주세요.
      placeholder: iPhone 14 Pro, iOS 16.0
