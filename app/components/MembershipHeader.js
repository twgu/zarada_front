"use client";

import { useRouter } from "next/navigation";
import { faArrowLeft } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

export default function MembershipHeader(props) {
  let title = props.title;
  let backDisabled = props.backDisabled;

  const router = useRouter();

  if (backDisabled != true) {
    backDisabled = false;
  }

  return (
    <header className="sub-header">
      {backDisabled ? null : (
        <button
          type="button"
          className="sub-back-btn"
          onClick={() => {
            router.back();
          }}
        >
          <FontAwesomeIcon icon={faArrowLeft} className="fas fa-arrow-left" />
        </button>
      )}
      <h2 className="sub-header-tit">{title}</h2>
    </header>
  );
}
