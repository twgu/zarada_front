"use client";

import { useRouter } from "next/navigation";
import { faArrowLeft } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

export default function MembershipHeader(props) {
  const router = useRouter();

  return (
    <header className="sub-header">
      <button
        type="button"
        className="sub-back-btn"
        onClick={() => {
          router.back();
        }}
      >
        <FontAwesomeIcon icon={faArrowLeft} className="fas fa-arrow-left" />
      </button>
      <h2 className="sub-header-tit">{props.title}</h2>
    </header>
  );
}
