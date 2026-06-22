import http from 'k6/http';
import { check, sleep } from 'k6';

// 캐시 애노테이션을 작성하기 전에 이 스크립트를 실행한다.
// 실행 명령: k6 run /scripts/before.js
// 이 결과가 "캐시 적용 전 기준선"이다. 이후 after.js 결과와 p95 응답 시간을 비교한다.
// 모든 요청이 애플리케이션을 거쳐 MySQL 조회까지 내려가므로 응답 시간이 안정적으로 높게 나온다.
export const options = {
  // 30명의 가상 사용자가 동시에 반복 요청을 보낸다.
  vus: 30,
  // 45초 동안 테스트해 캐시가 없는 상태의 평균적인 응답 분포를 관찰한다.
  duration: '45s',
  thresholds: {
    // 실패율이 1% 이상이면 성능 비교 전에 API 안정성부터 확인해야 한다.
    http_req_failed: ['rate<0.01'],
    // 캐시가 없으므로 p95 기준을 넉넉하게 둔다.
    http_req_duration: ['p(95)<900'],
  },
};

// BASE_URL 환경 변수를 주면 다른 포트나 서버에서도 같은 스크립트를 재사용할 수 있다.
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  // 900~919번 상품만 반복 조회하게 만들어 after.js에서 캐시 적중이 잘 보이게 한다.
  // 무작위 id를 너무 넓게 사용하면 캐시가 쌓이기 전에 계속 새로운 키만 조회하게 된다.
  const id = 900 + (__VU % 20);
  const detail = http.get(`${BASE_URL}/api/before/products/${id}`);
  check(detail, { 'detail ok': (r) => r.status === 200 });

  // 검색 조건도 항상 같게 유지한다. 그래야 캐시 적용 후 같은 검색 키가 반복된다.
  // popular와 DIGITAL은 더미 데이터에 충분히 존재하는 조합이다.
  const search = http.get(`${BASE_URL}/api/before/products?keyword=popular&category=DIGITAL`);
  check(search, { 'search ok': (r) => r.status === 200 });

  // sleep을 두면 순간 폭주보다 반복 사용자 패턴에 가까운 부하가 된다.
  sleep(1);
}
