"use client";

import { useRouter } from "next/navigation";
import Image from "next/image";
import { useEffect } from "react";

export default function App() {
  const router = useRouter();

  useEffect(() => {
    setTimeout(() => {
      router.push(`/pages/login`, { scroll: false });
    }, 1500);
  }, []);

  return (
    <div className="login-template">
      <div className="intro-page">
        <div className="intro-character">
          <Image
            src="/images/intro.png"
            width={250}
            height={310}
            alt="intro"
            priority={true}
          />
        </div>
        <p className="intro-copyright">
          Copyright 2023. DAI inc. all rights reserved.
        </p>
      </div>
    </div>
  );
}
