httpbin 
https://istio.io/latest/docs/tasks/traffic-management/ingress/ingress-control/#before-you-begin

https://istio.io/latest/docs/tasks/security/authentication/authn-policy/#require-a-valid-token

1. 불필요한 gateway 설치방지
istioctl install --set profile=minimal

2. 샘플 httpbin 어플리케이션 설치
kubectl apply -f samples/httpbin/httpbin.yaml

3. 01.httpbin-gateway.yaml (ns용 기본게이트웨이, 여기에선 default)
kubectl apply -f 01.httpbin-gateway.yaml

4. gateway http 라우팅설정
kubectl apply -f 02.httpbin-gateway-httproute.yaml

5. 테스트
CMD
curl -s -I -H "Host: httpbin.example.com" http://localhost:8080/status/200   <--성공
curl http://localhost/status/200 성공
단 브라우저에서는 안됨

6. 브라우저에서도 볼 수 있도록 Hostname 삭제
kubectl apply -f 03.httpbin-gateway-without-hostname-browser-ok.yaml

7. 브라우저 테스트 - ok
postman test - ok

8. JWT RequestAuthentication 적용
kubectl apply -f 04.jwt.yaml

9. 토큰발행 및 테스트
그냥 테스트 - 실패
토큰발행 및 테스트
CMD
for /f "delims=" %i in ('curl -s https://raw.githubusercontent.com/istio/istio/release-1.27/security/tools/jwt/samples/demo.jwt') do set TOKEN=%i

curl --header "Authorization: Bearer %TOKEN%" localhost/headers -s -o NUL -w "%%{http_code}\n"

성공

10. /headers에만 jwt적용
kubectl apply -f 05.jwt-authorization-policy.yaml

/status/200, /delay 모두 RBAC: access denied
/headers 는 됨


11. token정보를 header에 넣어주는 정책
token claims내 "foo"라는 값(값은 bar)을 X-jwt-Claim-Foo라는 header값에 넣어준다 
kubectl apply -f 06.token-claims-to-header.yaml



kubectl port-forward svc/httpbin-gateway-istio 8080:80

CMD
curl -s -I -H "Host: httpbin.example.com" http://localhost:8080/status/200
curl http://localhost/status/200


curl -s -I -H "Host: httpbin.example.com" http://localhost:8080/headers

curl %INGRESS_HOST%:%INGRESS_PORT%/headers -s -o NUL -w "%%{http_code}\n"
curl localhost/headers -s -o NUL -w "%%{http_code}\n"


curl --header "Authorization: Bearer deadbeef" "localhost/headers" -s -o NUL -w "%%{http_code}\n"
curl http://localhost/status/200


REM JWT 토큰 다운로드해서 TOKEN 변수에 저장
for /f "delims=" %i in ('curl -s https://raw.githubusercontent.com/istio/istio/release-1.27/security/tools/jwt/samples/demo.jwt') do set TOKEN=%i

REM Authorization 헤더 넣어서 요청
curl --header "Authorization: Bearer %TOKEN%" localhost/headers -s -o NUL -w "%%{http_code}\n"

