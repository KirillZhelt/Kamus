export default function RepositoryRoute(props) {
    return (
        <p>{props.match.params.owner}/{props.match.params.name}</p>
    );
}
