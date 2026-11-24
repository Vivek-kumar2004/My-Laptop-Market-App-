import { Link } from "react-router-dom";
import { Icon } from "./Icon";
import { routes } from "../lib/routes";

const WHATSAPP = "https://wa.me/919718199976";
const CONTACT = "https://mylaptopmarket.com/contact.html";
const INSTAGRAM = "https://www.instagram.com/mylaptopmarket/";
const TWITTER = "https://x.com/mylaptopmarket_";
const LINKEDIN = "https://www.linkedin.com/company/my-laptop-market/";

export function SideDock() {
  return (
    <aside
      aria-label="Quick links"
      className="pointer-events-none fixed left-3 top-1/2 z-[55] hidden -translate-y-1/2 xl:block"
    >
      <div className="pointer-events-auto flex flex-col items-center gap-1.5 rounded-[22px] border border-white/10 bg-[#0b0f16]/88 px-1.5 py-2 shadow-[0_24px_60px_-28px_rgba(5,7,11,0.75)] backdrop-blur-xl">
        <Link
          to={routes.home}
          className="flex h-10 w-10 items-center justify-center rounded-xl text-white/75 transition hover:bg-white/10 hover:text-white"
          aria-label="Home"
          title="Home"
        >
          <Icon name="grid" size={17} />
        </Link>

        <span className="my-0.5 h-px w-6 bg-white/10" aria-hidden />

        <a
          href={WHATSAPP}
          target="_blank"
          rel="noreferrer"
          className="flex h-10 w-10 items-center justify-center rounded-xl text-white/75 transition hover:bg-[#25D366]/20 hover:text-[#25D366]"
          aria-label="WhatsApp"
          title="WhatsApp"
        >
          <Icon name="whatsapp" size={17} />
        </a>
        <a
          href={CONTACT}
          className="flex h-10 w-10 items-center justify-center rounded-xl text-white/75 transition hover:bg-white/10 hover:text-white"
          aria-label="Contact / message"
          title="Contact"
        >
          <Icon name="message" size={17} />
        </a>
        <a
          href={INSTAGRAM}
          target="_blank"
          rel="noreferrer"
          className="flex h-10 w-10 items-center justify-center rounded-xl text-white/75 transition hover:bg-white/10 hover:text-white"
          aria-label="Instagram @mylaptopmarket"
          title="Instagram"
        >
          <Icon name="instagram" size={17} />
        </a>
        <a
          href={TWITTER}
          target="_blank"
          rel="noreferrer"
          className="flex h-10 w-10 items-center justify-center rounded-xl text-white/75 transition hover:bg-white/10 hover:text-white"
          aria-label="X @mylaptopmarket_"
          title="X / Twitter"
        >
          <Icon name="twitter" size={16} />
        </a>
        <a
          href={LINKEDIN}
          target="_blank"
          rel="noreferrer"
          className="flex h-10 w-10 items-center justify-center rounded-xl text-white/75 transition hover:bg-white/10 hover:text-white"
          aria-label="LinkedIn"
          title="LinkedIn"
        >
          <Icon name="linkedin" size={17} />
        </a>

        <span className="my-0.5 h-px w-6 bg-white/10" aria-hidden />

        <a
          href="https://mylaptopmarket.com/api/vendor/register"
          className="flex h-10 w-10 items-center justify-center rounded-full bg-brand text-white shadow-blue transition hover:bg-brand-bright hover:scale-105"
          aria-label="List your business"
          title="Register"
        >
          <Icon name="plus" size={18} strokeWidth={2.2} />
        </a>
      </div>
    </aside>
  );
}
