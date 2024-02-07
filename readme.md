<h1>pigeon-library</h1>

비둘기의 코틀린 기반 마인크래프트 플러그인 개발 라이브러리입니다.

---

<h2>사용법</h2>

1. 레포지토리를 클론하고, `mvn install`을 실행
2. 다른 레포지토리에서는 메이븐 로컬에 있는 `pigeon-library`를 가져오면 됨


<h2>참고</h2>

- 각 버전에 해당하는 remapped된 BuildTools가 필요할 수 있습니다. (`java -jar BuildTools.jar --rev 1.20.4 --remapped`) 
- 플러그인 실행 시 코틀린 라이브러리가 필요합니다. 플러그인 중 코틀린이 shaded된 플러그인이 없으면 [kotlin-plugin](https://github.com/Kobins/kotlin-plugin) 을 사용하세요.
- SpecialSource를 사용했습니다.