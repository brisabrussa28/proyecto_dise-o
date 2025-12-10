"use client"

import '../../css/Navbar.css';
import { FaSearch } from 'react-icons/fa';

const Navbar = ({ search = '', onSearchChange }) => {
  return (
    <div className="search-bar">
      <div className="search-section">
        <FaSearch className="icon" />
        <input
          className="search-input"
          type="text"
          placeholder="Buscar hechos..."
          value={search}
          onChange={(e) => onSearchChange?.(e.target.value)}
          aria-label="Buscar hechos"
        />
      </div>
    </div>
  );
};

export default Navbar;
