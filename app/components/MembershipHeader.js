"use client";

import { faArrowLeft } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useRouter } from "next/navigation";

export default function MembershipHeader(props) {
  const router = useRouter();

  const backPageControll = () => {
    switch (props.nowPage) {
      case "MembershipIdFind": //아이디 찾기
        router.push(`/pages/login/`, { scroll: false });
        break;
      case "MembershipIdFindSuccess": //아이디 찾기 성공
        router.push(`/pages/join/membershipIdFind/`, { scroll: false });
        break;
      case "MembershipIdFindFail": //아이디 찾기 실패
        router.push(`/pages/join/membershipIdFind/`, { scroll: false });
        break;
    }
  };

  return (
    <header className="sub-header">
      <button type="button" className="sub-back-btn" onClick={backPageControll}>
        <FontAwesomeIcon icon={faArrowLeft} className="fas fa-arrow-left" />
      </button>
      <h2 className="sub-header-tit">{props.title}</h2>
    </header>
  );
}
