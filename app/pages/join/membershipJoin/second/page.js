"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import axios from "axios";
import MembershipHeader from "@/app/components/MembershipHeader";
import FormGroup from "@/app/components/FormGroup";

export default function Second() {
  const router = useRouter();

  const [niceVal, setNiceVal] = useState(false);
  const [phoneVal, setPhoneVal] = useState("");
  const [nameVal, setNameVal] = useState("");
  const [postalCode, setPostalCode] = useState("");
  const [postalCodeError, setPostalCodeError] = useState(false);
  const [address, setAddress] = useState("");
  const [addressError, setAddressError] = useState(false);
  const [dtlAddress, setDtlAddress] = useState("");
  const [dtlAddressError, setDtlAddressError] = useState(false);

  useEffect(() => {
    if (phoneVal != "") {
      fUserDuplication();
    }
  }, [phoneVal]);

  const fNextStep = () => {
    if (!niceVal) {
      alert("휴대폰인증 후 진행하시기 바랍니다.");
      return;
    }

    if (postalCode == "") {
      setPostalCodeError(true);
      setAddressError(true);
      setDtlAddressError(true);
      return;
    }

    localStorage.setItem("join_userPhone", phoneVal);
    localStorage.setItem("join_userPhoneVal", phoneVal); // TODO : NICE 인증
    localStorage.setItem("join_userName", nameVal);
    localStorage.setItem("join_postalCode", postalCode);
    localStorage.setItem("join_userAddress", address);
    localStorage.setItem("join_userDetailAddress", dtlAddress);

    router.push("/pages/join/membershipJoin/third", {
      scroll: false,
    });
  };

  const fNicePhone = () => {
    // TODO : NICE 인증
    alert("나이스 휴대폰 인증 모듈 호출");
    setNiceVal(true);
    setPhoneVal("01023089903");
    setNameVal("구태완");
  };

  const fUserDuplication = () => {
    axios
      .post(process.env.NEXT_PUBLIC_API_URL + "/Phone_UserSelect", {
        userPhone: phoneVal,
        process: "join",
      })
      .then((response) => {
        if (response.data.length != 0) {
          if (response.data.id === undefined) {
            localStorage.setItem("join_mode", "Idcreate");
            localStorage.setItem("join_idcreateuser", response.data.userCo);
          } else {
            if (response.data.userStat == "A") {
              alert("이미회원가입이 되어있습니다. 로그인화면으로 이동합니다.");
            } else if (response.data.userStat == "N") {
              alert("이미회원가입이 되어있습니다. 간호사 승인 대기 중입니다.");
            }
            router.push("/pages/login", {
              scroll: false,
            });
          }
        }
      })
      .catch((error) => {
        alert(error.message);
      });
  };

  const onChangeHandler = (e, id) => {
    switch (id) {
      case "dtlAddress":
        if (e.target.value == "") {
          setDtlAddressError(true);
        } else {
          setDtlAddressError(false);
        }
        setDtlAddress(e.target.value);
        break;
    }
  };

  const onBlurHandler = (e, id) => {
    switch (id) {
      case "dtlAddress":
        if (dtlAddress == "") {
          setDtlAddressError(true);
        } else {
          setDtlAddressError(false);
        }
        break;
    }
  };

  const onClickHandler = (id) => {
    switch (id) {
      case "nicePhone":
        fNicePhone();
        break;
      case "addrSearch":
        // TODO : 다음 주소찾기
        alert("다음 주소찾기 모듈 호출");
        setPostalCode("04989");
        setAddress("서울 광진구 능동로32길 20-19");

        setPostalCodeError(false);
        setAddressError(false);
        break;
      case "next":
        fNextStep();
        break;
    }
  };

  return (
    <div className="h-project-template">
      <MembershipHeader title="회원가입" />
      <div className="h-project-content join-membership agree-membership">
        <div className="loading-bar second"></div>
        <h3 className="join-tit">
          보호자 정보를 적어주세요.
          <br />
        </h3>
        <p className="privacy-txt" style={{ fontSize: "11px" }}>
          <strong>
            (나이스평가정보에서 인증 받은 휴대폰 번호를 사용하고 있습니다.)
          </strong>
        </p>
        <button
          type="button"
          className="agree-right-btn3"
          onClick={(e) => {
            onClickHandler("nicePhone");
          }}
        >
          휴대폰 인증
        </button>
        {niceVal ? (
          <>
            <FormGroup error={false} errorText="">
              <div className="input-area">
                <input
                  type="text"
                  className="default-input"
                  id="agree-phone-number"
                  disabled={true}
                  placeholder="휴대폰 번호"
                  value={phoneVal}
                />
                <label htmlFor="agree-phone-number" className="blind">
                  휴대폰 번호
                </label>
              </div>
            </FormGroup>
            <FormGroup error={false} errorText="">
              <div className="input-area">
                <input
                  type="text"
                  className="default-input"
                  id="agree-guardian-name"
                  disabled={true}
                  placeholder="보호자 이름"
                  value={nameVal}
                />
                <label htmlFor="agree-guardian-name" className="blind">
                  보호자 이름
                </label>
              </div>
            </FormGroup>
          </>
        ) : null}
        <FormGroup error={postalCodeError} errorText="우편번호를 입력해주세요">
          <div className="input-area calc-input-area">
            <input
              type="text"
              className="default-input"
              id="postal-code"
              disabled={true}
              placeholder="우편번호"
              value={postalCode}
            />
            <label htmlFor="postal-code" className="blind">
              우편번호
            </label>
            <button
              type="button"
              className="agree-right-btn"
              onClick={(e) => {
                onClickHandler("addrSearch");
              }}
            >
              찾기
            </button>
          </div>
        </FormGroup>
        <FormGroup error={addressError} errorText="주소를 입력해주세요">
          <div className="input-area">
            <input
              type="text"
              className="default-input"
              id="agree-address"
              disabled={true}
              placeholder="주소"
              value={address}
            />
            <label htmlFor="agree-address" className="blind">
              주소
            </label>
          </div>
        </FormGroup>
        <FormGroup error={dtlAddressError} errorText="상세주소를 입력해주세요.">
          <div className="input-area">
            <input
              type="text"
              className="default-input"
              id="agree-detail-address"
              placeholder="상세주소"
              value={dtlAddress}
              onChange={(e) => {
                onChangeHandler(e, "dtlAddress");
              }}
              onBlur={(e) => {
                onBlurHandler(e, "dtlAddress");
              }}
            />
            <label htmlFor="agree-detail-address" className="blind">
              상세주소
            </label>
          </div>
        </FormGroup>
        <div className="bottom-fixed">
          <button
            type="button"
            className="default-block-btn agree-next-btn"
            onClick={(e) => {
              onClickHandler("next");
            }}
          >
            다 음
          </button>
        </div>
      </div>
    </div>
  );
}
