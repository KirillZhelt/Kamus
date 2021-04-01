import Navbar from 'react-bootstrap/Navbar';
import Nav from 'react-bootstrap/Nav';
import Button from 'react-bootstrap/Button';
import { NavLink } from 'react-router-dom';
import { Link } from 'react-router-dom';

function Header(props) {
  return (
    <Navbar bg='dark' variant='dark'>
        <Navbar.Brand>Watchdog</Navbar.Brand>
        <Navbar.Collapse id="basic-navbar-nav">
            <Nav className="mr-auto">
                <NavLink to="/" className="nav-link">Home</NavLink>
                <NavLink to="/" className="nav-link">Organizations</NavLink>
                <NavLink to="/" className="nav-link">Users</NavLink>
            </Nav>
            <Link to='/add-repository'><Button variant="outline-light" style={{marginRight: '20px'}}>Add repository</Button></Link>
        </Navbar.Collapse>
    </Navbar>
    );
}

export default Header;
