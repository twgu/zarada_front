#######################################################################
# [ 개발환경 ]

PS \zarada_front> node -v
v20.9.0

PS \zarada_front> npm -v
10.2.5

PS \zarada_front> npx create-next-app@latest .
√ Would you like to use TypeScript? ... No
√ Would you like to use ESLint? ... Yes
√ Would you like to use Tailwind CSS? ... No
√ Would you like to use 'src/' directory? ... No
√ Would you like to use App Router? (recommended) ... Yes
√ Would you like to customize the default import alias (@/*)? ... No
# react -> v18
# next -> v14.0.4

PS \zarada_front> npm i sass
PS \zarada_front> npm install axios
PS \zarada_front> npm install --save @fortawesome/react-fontawesome @fortawesome/free-solid-svg-icons
PS \zarada_front> npm install react-daum-postcode

#######################################################################
# [ 경로 설정 규칙 ]

# 1. .css 파일은 "../" 사용
# ex) "../../public/images/"

# 2. import 시 "@/" 사용
# ex) "@/app/components/FormGroup"

# 3. public 경로의 정적 파일 사용 시 또는 경로 라우팅 시 "/" 사용
# ex) "/images/intro.png"
# ex) "/pages/join/membershipJoin/first"
# ("/"는 "/app/" 경로와 "/public/" 경로 전부를 지칭함.)
