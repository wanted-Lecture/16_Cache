import http from 'k6/http';
import { check, sleep } from 'k6';

// @EnableCaching과 @Cacheable을 추가한 뒤 이 스크립트를 실행한다.
// 실행 명령: k6 run scripts/after.js
// before.js와 같은 접근 패턴을 사용하므로 성능 차이를 캐시 효과로 설명하기 쉽다.
// 첫 요청은 캐시 미스라서 느릴 수 있지만, 같은 키의 반복 요청은 Caffeine 캐시에서 빠르게 응답한다.
export const options = {
  // 캐시 적용 전과 같은 가상 사용자 수로 비교해야 공정하다.
  vus: 30,
  // 캐시 워밍업 이후의 응답 시간이 p95 지표에 반영된다.
  duration: '45s',
  thresholds: {
    // 캐시를 적용해도 실패율 기준은 낮게 유지해야 한다.
    http_req_failed: ['rate<0.01'],
    // 캐시 적용 후에는 p95 기준을 더 엄격하게 둔다.
    http_req_duration: ['p(95)<180'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  // before.js와 같은 id 범위를 사용한다. 같은 키 반복이 캐시 적중률을 만든다.
  // productDetail 캐시는 id별로 저장되므로 900~919번까지 최대 20개 캐시 항목이 생긴다.
  const id = 900 + (__VU % 20);
  const detail = http.get(`${BASE_URL}/api/after/products/${id}`);
  check(detail, { 'detail ok': (r) => r.status === 200 });

  // 검색 조건도 before.js와 동일하게 유지한다.
  // productSearch 캐시는 검색 조건 조합을 키로 사용하므로 이 요청은 하나의 검색 캐시 항목을 반복 조회한다.
  const search = http.get(`${BASE_URL}/api/after/products?keyword=popular&category=DIGITAL`);
  check(search, { 'search ok': (r) => r.status === 200 });

  sleep(1);
}
